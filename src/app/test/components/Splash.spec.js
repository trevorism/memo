import { mount } from "@vue/test-utils";
import HelloWorld from "./src/components/Splash.vue";
import { describe, it, expect, vi } from "vitest";


describe("Splash.vue", () => {

    it("should renders is page content is correct", () => {
        const wrapper = mount(HelloWorld, {
            props: {
                msg: "hello"
            }
        });

        expect(wrapper.text()).contains("hello");
    });

    it("should renders is page content is correct", () => {
        const wrapper = mount(HelloWorld, {
            props: {
                msg: "hello"
            }
        });

        expect(wrapper.vm.msg).toBe("hello");
    });

});