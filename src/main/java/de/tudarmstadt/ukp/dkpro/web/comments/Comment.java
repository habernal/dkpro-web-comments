/*
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
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

package de.tudarmstadt.ukp.dkpro.web.comments;

import java.util.Date;

/**
 * (c) 2015 Ivan Habernal
 */
public class Comment {

	private String text;

	private String id;

	private String parentId;

	private String previousPostId;

	private String commenterName;

	private boolean commenterTrusted;

	private String commenterLocation;

	private Date timestamp;

	private int recommendCount;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getPreviousPostId() {
		return previousPostId;
	}

	public void setPreviousPostId(String previousPostId) {
		this.previousPostId = previousPostId;
	}

	public String getCommenterName() {
		return commenterName;
	}

	public void setCommenterName(String commenterName) {
		this.commenterName = commenterName;
	}

	public boolean isCommenterTrusted() {
		return commenterTrusted;
	}

	public void setCommenterTrusted(boolean commenterTrusted) {
		this.commenterTrusted = commenterTrusted;
	}

	public String getCommenterLocation() {
		return commenterLocation;
	}

	public void setCommenterLocation(String commenterLocation) {
		this.commenterLocation = commenterLocation;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getRecommendCount() {
		return recommendCount;
	}

	public void setRecommendCount(int recommendCount) {
		this.recommendCount = recommendCount;
	}

	@Override
	public String toString() {
		return "Comment{" +
				"text='" + text + '\'' +
				", id='" + id + '\'' +
				", parentId='" + parentId + '\'' +
				", previousPostId='" + previousPostId + '\'' +
				", commenterName='" + commenterName + '\'' +
				", commenterTrusted=" + commenterTrusted +
				", commenterLocation='" + commenterLocation + '\'' +
				", timestamp=" + timestamp +
				", recommendCount=" + recommendCount +
				'}';
	}
}
