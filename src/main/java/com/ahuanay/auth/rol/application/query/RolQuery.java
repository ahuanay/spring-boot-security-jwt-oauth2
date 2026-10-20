package com.ahuanay.auth.rol.application.query;

import com.ahuanay.auth.common.application.query.PageableQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RolQuery extends PageableQuery {

    private String nombre;
}