package vn.bhxh.bhxhmail.message.extractors;


import android.support.annotation.NonNull;

import vn.bhxh.bhxhmail.helper.HtmlConverter;
import vn.bhxh.bhxhmail.mail.Message;
import vn.bhxh.bhxhmail.mail.Part;
import vn.bhxh.bhxhmail.mail.internet.MessageExtractor;
import vn.bhxh.bhxhmail.mail.internet.MimeUtility;


public class MessageFulltextCreator {
    private static final int MAX_CHARACTERS_CHECKED_FOR_FTS = 200*1024;


    private final TextPartFinder textPartFinder;
    private final EncryptionDetector encryptionDetector;


    MessageFulltextCreator(TextPartFinder textPartFinder, EncryptionDetector encryptionDetector) {
        this.textPartFinder = textPartFinder;
        this.encryptionDetector = encryptionDetector;
    }

    public static MessageFulltextCreator newInstance() {
        TextPartFinder textPartFinder = new TextPartFinder();
        EncryptionDetector encryptionDetector = new EncryptionDetector(textPartFinder);
        return new MessageFulltextCreator(textPartFinder, encryptionDetector);
    }

    public String createFulltext(@NonNull Message message) {
        if (encryptionDetector.isEncrypted(message)) {
            return null;
        }

        return extractText(message);
    }

    private String extractText(Message message) {
        Part textPart = textPartFinder.findFirstTextPart(message);
        if (textPart == null || hasEmptyBody(textPart)) {
            return null;
        }

        String text = MessageExtractor.getTextFromPart(textPart, MAX_CHARACTERS_CHECKED_FOR_FTS);
        String mimeType = textPart.getMimeType();
        if (!MimeUtility.isSameMimeType(mimeType, "text/html")) {
            return text;
        }

        return HtmlConverter.htmlToText(text);
    }

    private boolean hasEmptyBody(Part textPart) {
        return textPart.getBody() == null;
    }

}
