package vn.bhxh.bhxhmail.mailstore;


import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import vn.bhxh.bhxhmail.K9;
import vn.bhxh.bhxhmail.mail.Body;
import vn.bhxh.bhxhmail.mail.MessagingException;
import vn.bhxh.bhxhmail.mail.Multipart;
import vn.bhxh.bhxhmail.mail.Part;
import vn.bhxh.bhxhmail.message.extractors.AttachmentInfoExtractor;


/**
 * This class is used to encapsulate a message part, providing an interface to
 * get relevant info for a given Content-ID URI.
 *
 * The point of this class is to keep the Content-ID loading code agnostic of
 * the underlying part structure.
 */
public class AttachmentResolver {
    Map<String,Uri> contentIdToAttachmentUriMap;


    private AttachmentResolver(Map<String, Uri> contentIdToAttachmentUriMap) {
        this.contentIdToAttachmentUriMap = contentIdToAttachmentUriMap;
    }

    @Nullable
    public Uri getAttachmentUriForContentId(String cid) {
        return contentIdToAttachmentUriMap.get(cid);
    }

    @WorkerThread
    public static AttachmentResolver createFromPart(Part part) {
        AttachmentInfoExtractor attachmentInfoExtractor = AttachmentInfoExtractor.getInstance();
        Map<String, Uri> contentIdToAttachmentUriMap = buildCidToAttachmentUriMap(attachmentInfoExtractor, part);
        return new AttachmentResolver(contentIdToAttachmentUriMap);
    }

    @VisibleForTesting
    static Map<String,Uri> buildCidToAttachmentUriMap(AttachmentInfoExtractor attachmentInfoExtractor,
            Part rootPart) {
        HashMap<String,Uri> result = new HashMap<>();

        Stack<Part> partsToCheck = new Stack<>();
        partsToCheck.push(rootPart);

        while (!partsToCheck.isEmpty()) {
            Part part = partsToCheck.pop();

            Body body = part.getBody();
            if (body instanceof Multipart) {
                Multipart multipart = (Multipart) body;
                for (Part bodyPart : multipart.getBodyParts()) {
                    partsToCheck.push(bodyPart);
                }
            } else {
                try {
                    String contentId = part.getContentId();
                    if (contentId != null) {
                        AttachmentViewInfo attachmentInfo = attachmentInfoExtractor.extractAttachmentInfo(part);
                        result.put(contentId, attachmentInfo.uri);
                    }
                } catch (MessagingException e) {
                    Log.e(K9.LOG_TAG, "Error extracting attachment info", e);
                }
            }
        }

        return Collections.unmodifiableMap(result);
    }

}
