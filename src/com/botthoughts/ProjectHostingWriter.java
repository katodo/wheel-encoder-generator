/* Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.botthoughts;

import com.google.gdata.client.projecthosting.ProjectHostingService;
import com.google.gdata.data.HtmlTextConstruct;
import com.google.gdata.data.Person;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.projecthosting.Cc;
import com.google.gdata.data.projecthosting.IssuesEntry;
import com.google.gdata.data.projecthosting.Label;
import com.google.gdata.data.projecthosting.Owner;
import com.google.gdata.data.projecthosting.SendEmail;
import com.google.gdata.data.projecthosting.Status;
import com.google.gdata.data.projecthosting.Username;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Demonstrates how to use the Google Data API's Java client library to
 * interface with the Google Code Issue Tracker Data API.
 * There are examples for the following operations:
 *
 * <ol>
 * <li>Creating a new issue</li>
 * <li>Updating the issue by adding a comment with updates</li>
 * <li>Closing the issue by adding a comment with status "Fixed"</li>
 * </ol>
 *
 * 
 */
public class ProjectHostingWriter {

    /** Client that provides high level operations of the API */
    private ProjectHostingClient client;
    private ProjectHostingService service;
    private String username;

    /** Disable default public constructor */
    private ProjectHostingWriter() {
    }

    /**
     * Constructs the command line application.
     *
     * @throws AuthenticationException if authentication fails
     * @throws MalformedURLException if there's a problem with URL
     */
    public ProjectHostingWriter(
            ProjectHostingService service, String project, String username,
            String password) throws AuthenticationException, MalformedURLException {
        this.username = username;
        this.service = service;
        client = new ProjectHostingClient(service, project, username, password);
    }

    /**
     * Creates a new issue that can be inserted to the issues feed.
     */
    public IssuesEntry makeNewIssue(String type, String summary, String description) throws ServiceException, IOException {
        Person author = new Person();
        author.setName(username);

        Owner owner = new Owner();
        owner.setUsername(new Username(username));

        Cc cc = new Cc();
        cc.setUsername(new Username(username));

        IssuesEntry entry = new IssuesEntry();
        entry.getAuthors().add(author);

        // Uncomment the following line to set the owner along with issue creation.
        // It's intentionally commented out so we can demonstrate setting the owner
        // field using setOwnerUpdate() as shown in makeUpdatingComment() below.
        // entry.setOwner(owner);

        entry.setContent(new HtmlTextConstruct(description));
        entry.setTitle(new PlainTextConstruct(summary));
        entry.setStatus(new Status("New"));
        entry.addLabel(new Label("Type-"+type));
        entry.addLabel(new Label("Priority-Medium"));
        String os = PlatformUtilities.getOS();
        if (!os.equals(""))
            entry.addLabel(new Label("OpSys-"+os));
        entry.addCc(cc);
        entry.setSendEmail(new SendEmail("False"));

        return service.insert(client.getIssuesFeedUrl(), entry);
    }
}
