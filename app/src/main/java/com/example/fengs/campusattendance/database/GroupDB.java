package com.example.fengs.campusattendance.database;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;


public class GroupDB extends DataSupport {
    private int id;
    private String groupID;
    private String groupName;
    private List<Face> studentsGroup;

    public GroupDB(){
        studentsGroup = new ArrayList<>();
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Face> getStudentsGroup() {
        return studentsGroup;
    }

    public void setStudentsGroup(List<Face> studentsGroup) {
        this.studentsGroup = studentsGroup;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
