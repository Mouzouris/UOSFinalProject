package com.example.myapplication.Model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class RequestsModel {
    private String ID;
    private String PatientId;
    private String DoctorId;
    private Long timestamp;
    private String message;
    private String status;


    public RequestsModel(){}
    public RequestsModel(String ID, String PatientId, String DoctorId, Long timestamp,String status, String message) {
        this.ID = ID;
        this.PatientId = PatientId;
        this.DoctorId = DoctorId;
        this.timestamp = timestamp;
        this.message = message;
        this.status = status;
    }


    public String getID() { return ID;  }
    public void setID(String ID) {this.ID = ID; }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getPatientId() {
        return PatientId;
    }
    public void setPatientId(String patientId) {
        PatientId = patientId;
    }

    public String getDoctorId() {
        return DoctorId;
    }
    public void setDoctorId(String doctorId) {
        DoctorId = doctorId;
    }

    public Long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("ID", ID);
        result.put("PatientId", PatientId);
        result.put("DoctorId",DoctorId);
        result.put("Status",status);
        result.put("timestamp",timestamp);
        result.put("message", message);
        return result;
    }
    @Override
    public String toString() {
        return "Request: " +
                "ID= " + ID +
                "PatientId= " + PatientId +
                ", DoctorId= " + DoctorId +
                ", status= " + status +
                ", timestamp= " + timestamp +
                ", message= " + message;
    }


}




