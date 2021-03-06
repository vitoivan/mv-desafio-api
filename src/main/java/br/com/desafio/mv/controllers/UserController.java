package br.com.desafio.mv.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.desafio.mv.models.Breakfast;
import br.com.desafio.mv.models.User;
import br.com.desafio.mv.models.UserLogin;
import br.com.desafio.mv.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@CrossOrigin
	@PostMapping("")
	public ResponseEntity<?> createUser(@RequestBody User user)
	{	
		try {			
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			String encoded = passwordEncoder.encode(user.getPassword());
			user.setPassword(encoded);
			User userRegistered = userRepository.register(user.getName(), user.getCpf(), user.getPassword());
			return new ResponseEntity<User>(userRegistered, HttpStatus.CREATED);
		}
		catch (IllegalArgumentException e) {
			return new ResponseEntity<Object>("SENHA È OBRIGATÒRIA", HttpStatus.BAD_REQUEST);
		}
		catch (DataIntegrityViolationException e) {
			return new ResponseEntity<Object>("DADOS INVÀLIDOS OU USUÀRIO JÀ CADASTRADO", HttpStatus.BAD_REQUEST);
		}
	}
	
	@CrossOrigin
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody User user)
	{
		Optional<User> response = userRepository.login(user.getCpf());
		
		if (response.isEmpty())
		{
			return new ResponseEntity<Object>("CPF ou senha inválidos", HttpStatus.NOT_FOUND);
		}
		
		User userFound = response.get();
		BCryptPasswordEncoder passwordEnc = new BCryptPasswordEncoder();
		Boolean isTheSame =  passwordEnc.matches(user.getPassword(), userFound.getPassword());

		if (isTheSame)
		{
			UserLogin loggedUser = new UserLogin();
			loggedUser.setCpf(userFound.getCpf());
			loggedUser.setId(userFound.getId());
			loggedUser.setName(userFound.getName());
			return new ResponseEntity<UserLogin>(loggedUser, HttpStatus.OK);
		}
		return new ResponseEntity<Object>("CPF ou senha inválidos", HttpStatus.NOT_FOUND);
	}
	
	@CrossOrigin
	@GetMapping("/{id}")
	public ResponseEntity<?> getOne(@PathVariable("id") Long id){
		User item = userRepository.getOne(id);
		
		if (item == null) {
			return new ResponseEntity<String>("Não foi possível encontrar elemento", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<User>(item, HttpStatus.OK);
	}
}
