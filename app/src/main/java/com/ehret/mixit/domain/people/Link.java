/*
 * Copyright 2015 Guillaume EHRET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ehret.mixit.domain.people;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Lien partage par un speaker ou une personne
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Link {
    private String rel;
    private String href;

    public String getRel() {
        return rel;
    }

    public Link setRel(String rel) {
        this.rel = rel;
        return this;
    }

    public String getHref() {
        return href;
    }

    public Link setHref(String href) {
        this.href = href;
        return this;
    }
}
