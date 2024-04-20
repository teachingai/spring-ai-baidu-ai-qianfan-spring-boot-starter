package org.springframework.ai.baiduai.qianfan.aot;

import org.springframework.ai.baiduai.qianfan.BaiduAiQianfanChatOptions;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

public class BaiduAiQianfanRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        var mcs = MemberCategory.values();
        for (var tr : findJsonAnnotatedClassesInPackage(QianfanAiApi.class)) {
            hints.reflection().registerType(tr, mcs);
        }
        for (var tr : findJsonAnnotatedClassesInPackage(BaiduAiQianfanChatOptions.class)) {
            hints.reflection().registerType(tr, mcs);
        }
    }

}
