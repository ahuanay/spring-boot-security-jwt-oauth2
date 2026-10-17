package com.ahuanay.auth.permiso.application.query;

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
public class PermisoQuery extends PageableQuery {

    private String nombre;
}