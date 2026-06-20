package com.cotf.server.controller

import com.cotf.server.dto.AuthResponse
import com.cotf.server.dto.LoginRequest
import com.cotf.server.dto.RefreshRequest
import com.cotf.server.dto.RegisterRequest
import com.cotf.server.dto.UserDto
import com.cotf.server.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/signin")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ResponseEntity<AuthResponse> {
        val response = authService.refresh(request.refreshToken)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me")
    fun me(request: HttpServletRequest): ResponseEntity<UserDto> {
        val userId = UUID.fromString(request.getAttribute("userId") as String)
        val userDto = authService.getMe(userId)
        return ResponseEntity.ok(userDto)
    }
}
