{{- define "service-auth.fullname" -}}
{{- printf "%s-auth" .Release.Name -}}
{{- end -}}
