package thomasWeise.tools;

import java.lang.ProcessBuilder.Redirect;

/** An enumeration of ways to access the stream of a process */
public enum EProcessStream {

  /** The process stream is accessed as stream. */
  AS_STREAM(Redirect.PIPE),

  /** All data coming from or going to the stream is ignored. */
  IGNORE(Redirect.PIPE),

  /** The stream is redirected to a file/path. */
  REDIRECT_TO_PATH(null),

  /** Inherit the stream from the calling process. */
  INHERIT(Redirect.INHERIT);

  /** the redirection */
  final Redirect m_redir;

  /**
   * create
   *
   * @param redir
   *          the redirect
   */
  EProcessStream(final Redirect redir) {
    this.m_redir = redir;
  }
}
