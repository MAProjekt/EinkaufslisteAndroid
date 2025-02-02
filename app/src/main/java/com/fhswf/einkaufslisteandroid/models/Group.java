package com.fhswf.einkaufslisteandroid.models;

import java.util.List;

public class Group {
    private String groupId;
    private String groupName;
    private String createdBy; // vlt. für spätere Spezifikationen
    private List<String> memberUid;

    private List<Product> products; // vlt Alternative

    public Group(){

    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setMemberUid(List<String> memberUid) {
        this.memberUid = memberUid;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public List<String> getMemberUid() {
        return memberUid;
    }

    public List<Product> getProducts() {
        return products;
    }
}
