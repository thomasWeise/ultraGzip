package thomasWeise.tools;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A builder for external processes.
 */
public final class ExternalProcessBuilder
    implements Supplier<ExternalProcess> {

  /** the command */
  private final ArrayList<String> m_command;
  /** the process builder */
  private final ProcessBuilder m_pb;

  /** the stdin stream definition */
  private EProcessStream m_stdin;
  /** the stdout stream definition */
  private EProcessStream m_stdout;
  /** the stderr stream definition */
  private EProcessStream m_stderr;

  /** create the process builder */
  ExternalProcessBuilder() {
    super();
    this.m_command = new ArrayList<>();
    this.m_pb = new ProcessBuilder(this.m_command);

    this.setStdErr(EProcessStream.AS_STREAM);
    this.setStdOut(EProcessStream.AS_STREAM);
    this.setStdIn(EProcessStream.AS_STREAM);
  }

  /**
   * Set the executable
   *
   * @param path
   *          the path to the executable
   * @return this builder
   */
  public final ExternalProcessBuilder
      setExecutable(final Path path) {
    String s;

    s = IOUtils.canonicalizePath(path).toString();
    if (this.m_command.isEmpty()) {
      this.m_command.add(s);
    } else {
      this.m_command.set(0, s);
    }

    return this;
  }

  /**
   * Add a string command line argument
   *
   * @param s
   *          the string command line argument
   * @return this builder
   */
  public final ExternalProcessBuilder
      addStringArgument(final String s) {
    if (this.m_command.isEmpty()) {
      throw new IllegalStateException(//
          "Must first set command, then can add arguments."); //$NON-NLS-1$
    }
    if (s == null) {
      throw new IllegalArgumentException(//
          "Command line argument cannot be null."); //$NON-NLS-1$
    }
    this.m_command.add(s);
    return this;
  }

  /**
   * Add a path command line argument
   *
   * @param path
   *          the path command line argument
   * @return this builder
   */
  public final ExternalProcessBuilder
      addPathArgument(final Path path) {
    return this.addStringArgument(path.normalize().toString());
  }

  /**
   * Set a string environment variable for the sub-process
   *
   * @param key
   *          the key
   * @param value
   *          the value
   * @return this builder
   */
  public final ExternalProcessBuilder putEnvironmentString(
      final String key, final String value) {
    this.m_pb.environment().put(key, value);
    return this;
  }

  /**
   * Set a path environment variable for the sub-process
   *
   * @param key
   *          the key
   * @param value
   *          the value
   * @return this builder
   */
  public final ExternalProcessBuilder
      putEnvironmentPath(final String key, final Path value) {
    final String s;
    s = IOUtils.canonicalizePath(value).toString();
    return this.putEnvironmentString(key, s);
  }

  /**
   * Remove an environment variable
   *
   * @param key
   *          the variable to remove
   * @return this builder
   */
  public final ExternalProcessBuilder
      removeEnvironmentVar(final String key) {
    this.m_pb.environment().remove(key);
    return this;
  }

  /**
   * Clear the environment, i.e., delete all variables
   *
   * @return this builder
   */
  public final ExternalProcessBuilder clearEnvironment() {
    this.m_pb.environment().clear();
    return this;
  }

  /**
   * Set the directory in which the process should be executed
   *
   * @param dir
   *          the directory
   * @return this builder
   */
  public final ExternalProcessBuilder
      setDirectory(final Path dir) {
    this.m_pb.directory(IOUtils.canonicalizePath(dir).toFile()
        .getAbsoluteFile());
    return this;
  }

  /**
   * Set the stdin stream definition
   *
   * @param def
   *          the stream definition
   * @return this builder
   */
  public final ExternalProcessBuilder
      setStdIn(final EProcessStream def) {
    final Redirect redirect;
    if ((def != null) && ((redirect = def.m_redir) != null)) {
      this.m_pb.redirectInput(redirect);
      this.m_stdin = def;
      return this;
    }
    throw new IllegalArgumentException(
        "Cannot set stdin to " + def); //$NON-NLS-1$
  }

  /**
   * Read the stdin of this process from the given path
   *
   * @param source
   *          the source
   * @return this builder
   */
  public final ExternalProcessBuilder
      readStdInFrom(final Path source) {
    this.m_pb.redirectInput(
        source.normalize().toFile().getAbsoluteFile());
    this.m_stdin = EProcessStream.REDIRECT_TO_PATH;
    return this;
  }

  /**
   * Set the stdout stream definition
   *
   * @param def
   *          the stream definition
   * @return this builder
   */
  public final ExternalProcessBuilder
      setStdOut(final EProcessStream def) {
    final Redirect redirect;
    if ((def != null) && ((redirect = def.m_redir) != null)) {
      this.m_pb.redirectOutput(redirect);
      this.m_stdout = def;
      return this;
    }
    throw new IllegalArgumentException(
        "Cannot set stdout to " + def); //$NON-NLS-1$
  }

  /**
   * Store the stdout of this process to the given path
   *
   * @param dest
   *          the destination
   * @param append
   *          should we append to the file identified by
   *          {@code dest} if it exists ({@code true}) or
   *          overwrite it ({@code false})?
   * @return this builder
   */
  public final ExternalProcessBuilder
      writeStdOutTo(final Path dest, final boolean append) {
    final File file;
    final Redirect redir;

    file = IOUtils.canonicalizePath(dest).toFile()
        .getAbsoluteFile();
    this.m_pb.redirectOutput(redir =
        (append ? Redirect.appendTo(file) : Redirect.to(file)));
    this.m_stdout = EProcessStream.REDIRECT_TO_PATH;

    if (this.m_pb.redirectErrorStream()) {
      this.m_stderr = this.m_stdout;
      this.m_pb.redirectError(redir);
    }
    return this;
  }

  /** check redirection */
  private final void __checkRedirect() {
    if (this.m_pb.redirectErrorStream()) {
      throw new IllegalArgumentException(//
          "Stderr is redirected to stdout, so its stream mode cannot be modified anymore."); //$NON-NLS-1$
    }
  }

  /**
   * Set the stderr stream definition
   *
   * @param def
   *          the stream definition
   * @return this builder
   */
  public final ExternalProcessBuilder
      setStdErr(final EProcessStream def) {
    final Redirect redirect;

    if ((def != null) && ((redirect = def.m_redir) != null)) {
      this.__checkRedirect();
      this.m_pb.redirectError(redirect);
      this.m_stderr = def;
      return this;
    }
    throw new IllegalArgumentException(
        "Cannot set stderr to " + def); //$NON-NLS-1$
  }

  /**
   * Store the stderr of this process to the given path
   *
   * @param dest
   *          the destination
   * @param append
   *          should we append to the file identified by
   *          {@code dest} if it exists ({@code true}) or
   *          overwrite it ({@code false})?
   * @return this builder
   */
  public final ExternalProcessBuilder
      writeStdErrTo(final Path dest, final boolean append) {
    final File file;

    this.__checkRedirect();
    file = IOUtils.canonicalizePath(dest).toFile()
        .getAbsoluteFile();
    this.m_pb.redirectError(//
        append ? Redirect.appendTo(file) : Redirect.to(file));
    this.m_stderr = EProcessStream.REDIRECT_TO_PATH;
    return this;
  }

  /**
   * validate the stream merge
   *
   * @param merge
   *          the merge
   */
  private final void __validateMerge(final boolean merge) {
    Redirect out, err;
    Redirect.Type t1, t2;
    File f;

    out = this.m_pb.redirectOutput();
    err = this.m_pb.redirectError();

    if (merge) {
      if (this.m_stderr != this.m_stdout) {
        throw new IllegalStateException(//
            "If you merge stdout and stderr, they cannot have different stream modes, but stdout has " //$NON-NLS-1$
                + this.m_stdout + " and stderr has " //$NON-NLS-1$
                + this.m_stderr);
      }
      if (!(Objects.equals(err, out))) {
        throw new IllegalStateException(//
            "If you merge stdout and stderr, they cannot have different redirects, but stdout has " //$NON-NLS-1$
                + this.m_pb.redirectOutput() + " and stderr has " //$NON-NLS-1$
                + this.m_pb.redirectError());
      }
    } else {
      if ((((t1 = out.type()) == Redirect.Type.APPEND)
          || (t1 == Redirect.Type.WRITE))
          && (((t2 = err.type()) == Redirect.Type.APPEND)
              || (t2 == Redirect.Type.WRITE))
          && Objects.equals((f = out.file()), err.file())) {
        throw new IllegalStateException(//
            "If you do not merge stdout and stderr, they cannot be redirected to the same file " //$NON-NLS-1$
                + t1 + " and stderr has " //$NON-NLS-1$
                + t2 + " and both redirect to '" + //$NON-NLS-1$
                f + '\'');
      }
    }
  }

  /**
   * Should stdout and stderr be merged?
   *
   * @param merge
   *          {@code true} if stdout and stderr should be merged,
   *          {@code false} if they are separate streams
   * @return this builder
   */
  public final ExternalProcessBuilder
      setMergeStdOutAndStdErr(final boolean merge) {
    this.__validateMerge(merge);
    this.m_pb.redirectErrorStream(merge);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("fallthrough")
  public final ExternalProcess get() {
    final ExternalProcess external;
    final Process process;
    final boolean merge;
    int realStreams;
    // checking validity
    if (this.m_command.size() <= 0) {
      throw new IllegalArgumentException(//
          "Must specify program to execute."); //$NON-NLS-1$
    }
    if (this.m_stdin == null) {
      throw new IllegalArgumentException(//
          "Must select treatment for stdin."); //$NON-NLS-1$
    }
    if (this.m_stdout == null) {
      throw new IllegalArgumentException(//
          "Must select treatment for stdout."); //$NON-NLS-1$
    }
    if (this.m_stderr == null) {
      throw new IllegalArgumentException(//
          "Must select treatment for stderr."); //$NON-NLS-1$
    }
    this.__validateMerge(this.m_pb.redirectErrorStream());

    // ok, everything seems to be OK
    try {
      process = this.m_pb.start();
    } catch (final IOException ioe) {
      ConsoleIO.stderr("Error when starting " + //$NON-NLS-1$
          this.m_command, ioe); // $NON-NLS-1$
      throw new RuntimeException(ioe);
    }

    external =
        new ExternalProcess(process, this.m_command.get(0));

    realStreams = 0;

    // setup standard in
    external.m_stdin = process.getOutputStream();
    switch (this.m_stdin) {
      case AS_STREAM: {
        realStreams++;
        break;
      }

      case IGNORE: {
        // Since we do not write to stdin, we can close it right
        // away to
        // let the child process know.
        // TODO: This could cause a problem, if there are odd
        // errors, try
        // to revise this piece of code.
        try {
          external.m_stdin.close();
        } catch (final Throwable error) {
          ConsoleIO.stderr(
              "strange error when closing process input stream", //$NON-NLS-1$
              error);
        }
        // fall though to NullOutputStream.INSTANCE
      }

      default: {
        external.m_stdin = NullOutputStream.INSTANCE;
        break;
      }
    }

    // setup standard out
    external.m_stdout = process.getInputStream();
    switch (this.m_stdout) {
      case AS_STREAM: {
        realStreams++;
        break;
      }

      case IGNORE: {
        external.m_stdoutWorker =
            new _DiscardInputStream(external.m_stdout);
        // fall though to NullInputStream.INSTANCE
      }
      default: {
        external.m_stdout = NullInputStream.INSTANCE;
        break;
      }
    }

    // setup standard err
    merge = this.m_pb.redirectErrorStream();
    if (merge) {
      external.m_stderr = external.m_stdout;
    } else {
      external.m_stderr = process.getErrorStream();
      switch (this.m_stderr) {
        case AS_STREAM: {
          realStreams++;
          break;
        }

        case IGNORE: {
          external.m_stderrWorker =
              new _DiscardInputStream(external.m_stderr);
          // fall though to NullInputStream.INSTANCE
        }
        default: {
          external.m_stderr = NullInputStream.INSTANCE;
          break;
        }
      }
    }

    // If we have more than one real stream from which we read or
    // to which
    // we write, we need to deal with the potential of deadlocks
    // due to
    // full pipes. In other words, a single thread can never
    // reliably ready
    // from more than one stream. This becomes harakiri with
    // readers
    // sitting on top of streams. We can solve this by placing
    // (unlimited)
    // buffers between the real stream and the stream we let the
    // user read
    // from and shovel the data over with worker threads.
    if (realStreams > 1) {

      if (this.m_stdin == EProcessStream.AS_STREAM) {
        external.m_stdinBuffer =
            new ByteProducerConsumerBuffer();
        external.m_stdinWorker = new _BufferToOutputStream(
            external.m_stdin, external.m_stdinBuffer);
        external.m_stdin = new _ProducerConsumerOutputStream(
            external.m_stdinBuffer);
      }

      if (this.m_stdout == EProcessStream.AS_STREAM) {
        external.m_stdoutBuffer =
            new ByteProducerConsumerBuffer();
        external.m_stdoutWorker = new _InputStreamToBuffer(
            external.m_stdoutBuffer, external.m_stdout);
        external.m_stdout = new _ProducerConsumerInputStream(
            external.m_stdoutBuffer);
      }

      if (merge) {
        external.m_stderr = external.m_stdout;
      } else {
        if (this.m_stderr == EProcessStream.AS_STREAM) {
          external.m_stderrBuffer =
              new ByteProducerConsumerBuffer();
          external.m_stderrWorker = new _InputStreamToBuffer(
              external.m_stderrBuffer, external.m_stderr);
          external.m_stderr = new _ProducerConsumerInputStream(
              external.m_stderrBuffer);
        }
      }

    }

    external._start();
    return external;
  }
}
