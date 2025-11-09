package montclio.heimdall.security;

import montclio.heimdall.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // 1. Permissões (Roles): Converte sua 'UserCategory' para uma 'GrantedAuthority' do Spring
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Assumindo que userCategory.getCategory() retorna "ADMIN" ou "OPERADOR"
        String roleName = user.getUserCategory().getCategory();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    // 2. Senha: Retorna o hash que está no banco
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // 3. Username: Usaremos o EMAIL como login
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // 4. Controles de Conta (Deixamos tudo 'true' para simplificar)
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}