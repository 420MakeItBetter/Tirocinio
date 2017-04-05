package client.messages;

/**
 * Created by Matteo on 08/10/2016.
 *
 */
public enum CodeTypes {
    REJECT_MALFORMED,
    REJECT_INVALID,
    REJECT_OBSOLETE,
    REJECT_DUPLICATE,
    REJECT_NONSTANDARD,
    REJECT_DUST,
    REJECT_INSUFFICIENTFEE,
    REJECT_CHECKPOINT
}
