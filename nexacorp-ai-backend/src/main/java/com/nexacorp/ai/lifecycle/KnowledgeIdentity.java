package com.nexacorp.ai.lifecycle;

import com.nexacorp.ai.lifecycle.model.KnowledgeRequest;
import com.nexacorp.ai.lifecycle.model.SourceType;

public class KnowledgeIdentity {

    public static String from(KnowledgeRequest request) {

        if (request.getSourceType() == SourceType.PDF) {
            return "PDF#" + request.getName();
        }

        if (request.getSourceType() == SourceType.WIKI) {
            return "WIKI#" + request.getName();
        }

        if (request.getSourceType() == SourceType.DATABASE) {
            return "DB#" + request.getName();
        }

        return "";
    }
}
