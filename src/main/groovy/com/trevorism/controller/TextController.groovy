package com.trevorism.controller

import com.stripe.Stripe
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import com.trevorism.ClasspathBasedPropertiesProvider
import com.trevorism.PropertiesProvider
import com.trevorism.gcloud.webapi.model.MemoMessage
import com.trevorism.gcloud.webapi.model.TextSubmission
import com.trevorism.gcloud.webapi.service.DefaultMessageService
import com.trevorism.gcloud.webapi.service.MessageService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.swagger.v3.oas.annotations.tags.Tag

import java.util.logging.Logger

@Controller("/api/text")
class TextController {

    private static final Logger log = Logger.getLogger(TextController.class.name)
    public static final BigDecimal COST_IN_USD = 1.99
    public static final int PREVIEW_LENGTH = 10
    private PropertiesProvider propertiesProvider = new ClasspathBasedPropertiesProvider()
    private MessageService messageService = new DefaultMessageService()

    @Tag(name = "Text Operations")
    @Post(value = "/submission", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    Map submitText(@Body TextSubmission textSubmission) {
        log.info(textSubmission.toString())
        Stripe.apiKey = propertiesProvider.getProperty("stripeApiKey")

        String text = textSubmission.getText()
        int length = text.length() < PREVIEW_LENGTH ? text.length() : PREVIEW_LENGTH
        String preview = text[0..length - 1] + "..."

        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder().setName(preview).build()
        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                .setUnitAmount((long) (COST_IN_USD * 100))
                .setProductData(productData)
                .build()
        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(priceData)
                .build()

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://memowand.com/paymentsuccess")
                .setCancelUrl("https://memowand.com/")
                .addLineItem(lineItem)
                .build()

        messageService.saveMessage(new MemoMessage(type: "text", content: text))
        Session session = Session.create(params)
        return [id: session.getId()]
    }
}
