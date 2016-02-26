package thomasWeise.ultraGzip;

/** The outcome of the registration of a gzip result. */
enum _ERegistrationResult {
  /** this is the new best result */
  IMPROVEMENT,
  /** we already have a better result */
  NO_IMPROVEMENT,
  /** the data was invalid */
  INVALID;
}
