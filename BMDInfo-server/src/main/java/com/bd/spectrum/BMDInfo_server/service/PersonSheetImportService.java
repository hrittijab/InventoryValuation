package com.bd.spectrum.BMDInfo_server.service;


import com.bd.spectrum.BMDInfo_server.model.Person;
import com.bd.spectrum.BMDInfo_server.repository.PersonRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Service
public class PersonSheetImportService {

    private final PersonRepository personRepository;

    public PersonSheetImportService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void importSheetData() throws Exception {
        Sheets sheets = getSheetsService();
        String spreadsheetId = "1HxS-bQ1quAmwZDDQjrPl5DvayQKVFSFh8b51fyDZ6vc";
        String range = "Sheet1!A2:B";

        ValueRange response = sheets.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> rows = response.getValues();

        if (rows != null) {
            for (List<Object> row : rows) {
                String name = row.size() > 0 ? row.get(0).toString() : "";
                String email = row.size() > 1 ? row.get(1).toString() : "";

                Person person = new Person(null, name, email);
                personRepository.save(person);
            }
        }
    }

    private Sheets getSheetsService() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("credentials.json");

        if (inputStream == null) {
            throw new FileNotFoundException("credentials.json not found in resources");
        }

        GoogleCredential credential = GoogleCredential.fromStream(inputStream)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

        return new Sheets.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
                .setApplicationName("Google Sheets Import")
                .build();
    }


}
