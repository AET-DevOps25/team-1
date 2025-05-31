{{- define "service-application.fullname" -}}
{{- printf "%s-application" .Release.Name -}}
{{- end -}}
