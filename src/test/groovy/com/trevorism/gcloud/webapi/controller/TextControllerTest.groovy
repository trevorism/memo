package com.trevorism.gcloud.webapi.controller

import org.junit.Test


class TextControllerTest {

    @Test
    void submitText() {
        String textSubmission = "hellohello1"
        int length = textSubmission.length() < TextController.PREVIEW_LENGTH ? textSubmission.length() : TextController.PREVIEW_LENGTH

        String preview = textSubmission[0..length - 1] + "..."
        assert "hellohello..." == preview
    }
}
