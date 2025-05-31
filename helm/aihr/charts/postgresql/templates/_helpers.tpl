{{- define "postgresql.fullname" -}}
{{- printf "%s-postgresql" .Release.Name -}}
{{- end -}}
