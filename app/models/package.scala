
package object models {

  /** Messages used in the app. */
  object Messages {

    /** Message to get all data stored in an actor state. */
    object GetAll

    /** Message to delete a venue from the [[actors.VenueActor]].
      *
      * @constructor create new DeleteVenue with venueID
      * @param venueID venue ID to be deleted.
      */
    case class DeleteVenue(venueID: VenueID)

    /** Operation Notification Trait. */
    trait Notification extends Any

    /** Message to mark an operation as successful.
      *
      * @constructor create new SuccessNotifier with a message
      * @param message success message
      */
    case class SuccessNotification(message: String) extends AnyVal with Notification

    /** Message to mark an operation as failed.
      *
      * @constructor create new FailureNotifier with a message
      * @param message failure message
      */
    case class FailureNotification(message: String) extends AnyVal with Notification

    /** Purchase Status Trait. */
    trait PurchaseStatus

    /** Reason why purchase failed.
      *
      * @constructor create new PurchaseFailed with a reason
      * @param reason purchase failure reason
      */
    case class PurchaseFailed(reason: String) extends PurchaseStatus

    /** Purchase status indicating success and holding
      * the purchased venue.
      *
      * @constructor create new PurchaseSuccess with purchased venue
      * @param venue purchased venue
      */
    case class PurchaseSucceeded(venue: Venue) extends PurchaseStatus
  }
}
