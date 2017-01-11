/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.client.entities.impl;

import net.dv8tion.jda.client.JDAClient;
import net.dv8tion.jda.client.entities.*;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class JDAClientImpl implements JDAClient
{
    protected final JDAImpl api;
    protected final HashMap<String, Group> groups = new HashMap<>();
    protected final HashMap<String, Relationship> relationships = new HashMap<>();
    protected final HashMap<String, CallUser> callUsers = new HashMap<>();
    protected UserSettingsImpl userSettings;

    public JDAClientImpl(JDAImpl api)
    {
        this.api = api;
        this.userSettings = new UserSettingsImpl(api);
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public List<Group> getGroups()
    {
        return Collections.unmodifiableList(
                new ArrayList<>(
                        groups.values()));
    }

    @Override
    public List<Group> getGroupsByName(String name, boolean ignoreCase)
    {
        return Collections.unmodifiableList(groups.values().stream()
                .filter(g -> g.getName() != null
                        && (ignoreCase
                            ? g.getName().equalsIgnoreCase(name)
                            : g.getName().equals(name)))
                .collect(Collectors.toList()));
    }

    @Override
    public Group getGroupById(String id)
    {
        return groups.get(id);
    }

    @Override
    public List<Relationship> getRelationships()
    {
        return Collections.unmodifiableList(
                new ArrayList<>(
                        relationships.values()));
    }

    @Override
    public List<Relationship> getRelationships(RelationshipType type)
    {
        return Collections.unmodifiableList(relationships.values().stream()
                .filter(r -> r.getType().equals(type))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Relationship> getRelationships(RelationshipType type, String name, boolean ignoreCase)
    {
        return Collections.unmodifiableList(relationships.values().stream()
                .filter(r -> r.getType().equals(type))
                .filter(r -> (ignoreCase
                        ? r.getUser().getName().equalsIgnoreCase(name)
                        : r.getUser().getName().equals(name)))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Relationship> getRelationshipsByName(String name, boolean ignoreCase)
    {
        return Collections.unmodifiableList(relationships.values().stream()
                .filter(r -> (ignoreCase
                        ? r.getUser().getName().equalsIgnoreCase(name)
                        : r.getUser().getName().equals(name)))
                .collect(Collectors.toList()));
    }

    @Override
    public Relationship getRelationship(User user)
    {
        return getRelationshipById(user.getId());
    }

    @Override
    public Relationship getRelationship(Member member)
    {
        return getRelationship(member.getUser());
    }

    @Override
    public Relationship getRelationshipById(String id)
    {
        return relationships.get(id);
    }

    @Override
    public Relationship getRelationshipById(String id, RelationshipType type)
    {
        Relationship relationship = getRelationshipById(id);
        if (relationship != null && relationship.getType() == type)
            return relationship;
        else
            return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Friend> getFriends()
    {
        return (List<Friend>) (List) getRelationships(RelationshipType.FRIEND);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Friend> getFriendsByName(String name, boolean ignoreCase)
    {
        return (List<Friend>) (List) getRelationships(RelationshipType.FRIEND, name, ignoreCase);
    }

    @Override
    public Friend getFriend(User user)
    {
        return getFriendById(user.getId());
    }

    @Override
    public Friend getFriend(Member member)
    {
        return getFriend(member.getUser());
    }

    @Override
    public Friend getFriendById(String id)
    {
        return (Friend) getRelationshipById(id, RelationshipType.FRIEND);
    }

    @Override
    public UserSettings getSettings()
    {
        return userSettings;
    }

    public HashMap<String, Group> getGroupMap()
    {
        return groups;
    }

    public HashMap<String, Relationship> getRelationshipMap()
    {
        return relationships;
    }

    public HashMap<String, CallUser> getCallUserMap()
    {
        return callUsers;
    }

    @Override
    public RestAction<List<Application>> getApplications()
    {
        return new RestAction<List<Application>>(api, Route.Applications.GET_APPLICATIONS.compile(), null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                {
                    JSONArray array = response.getArray();
                    List<Application> applications = new ArrayList<>(array.length());
                    EntityBuilder entityBuilder = EntityBuilder.get(getJDA());

                    for (int i = 0; i < array.length(); i++)
                        applications.add(entityBuilder.createApplication(array.getJSONObject(i)));

                    request.onSuccess(applications);
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public RestAction<Application> getApplicationById(String id)
    {
        return new RestAction<Application>(api, Route.Applications.GET_APPLICATION.compile(id), null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(EntityBuilder.get(getJDA()).createApplication(response.getObject()));
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<List<AuthorizedApplication>> getAuthorizedApplications()
    {
        return new RestAction<List<AuthorizedApplication>>(api,
                Route.Applications.GET_AUTHORIZED_APPLICATIONS.compile(), null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                {
                    JSONArray array = response.getArray();
                    List<AuthorizedApplication> applications = new ArrayList<>(array.length());
                    EntityBuilder entityBuilder = EntityBuilder.get(getJDA());

                    for (int i = 0; i < array.length(); i++)
                        applications.add(entityBuilder.createAuthorizedApplication(array.getJSONObject(i)));

                    request.onSuccess(applications);
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public RestAction<AuthorizedApplication> getAuthorizedApplicationById(String id)
    {
        return new RestAction<AuthorizedApplication>(api, Route.Applications.GET_AUTHORIZED_APPLICATION.compile(id),
                null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(EntityBuilder.get(getJDA()).createAuthorizedApplication(response.getObject()));
                else
                    request.onFailure(response);
            }
        };
    }
}
