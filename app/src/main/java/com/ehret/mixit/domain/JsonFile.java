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
package com.ehret.mixit.domain;

/**
 * Differents fichiers Json recuperes sur le site MiXiT
 */
public enum JsonFile {
    FileStaff(TypeFile.staff, "https://mixitconf.org/api/staff", false),
    FileTalks(TypeFile.talks, "https://mixitconf.org/api/talk", false),
    FileEvent(TypeFile.sponsor, "https://mixitconf.org/api/event/mixit17" , false),
    FileUser(TypeFile.user, "https://mixitconf.org/api/user", false);

    private String url;
    private TypeFile type;
    private boolean readRemote;

    private JsonFile(TypeFile type, String url, boolean readRemote) {
        this.url = url;
        this.type = type;
        this.readRemote = readRemote;
    }

    public String getUrl() {
        return url;
    }

    public TypeFile getType() {
        return type;
    }

    public boolean isReadRemote() {
        return readRemote;
    }
}
