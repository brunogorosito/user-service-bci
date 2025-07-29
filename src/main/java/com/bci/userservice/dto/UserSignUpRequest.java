package com.bci.userservice.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

public class UserSignUpRequest {

    private String name;

    @NotBlank(message = "Email es requerido")
    @Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z]{2,}$",
            message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "Password es requerido")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d.*\\d)[a-zA-Z\\d]{8,12}$",
            message = "Password debe tener una mayúscula, dos números, solo letras y números, entre 8-12 caracteres")
    private String password;

    private List<PhoneDto> phones;

    // Constructores, getters y setters
    public UserSignUpRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<PhoneDto> getPhones() { return phones; }
    public void setPhones(List<PhoneDto> phones) { this.phones = phones; }
}