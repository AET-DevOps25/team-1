{{- define "service-application.name" -}}
service-application
{{- end }}

{{- define "service-application.fullname" -}}
{{- printf "%s-application" .Release.Name -}}
{{- end -}}

{{- define "service-application.selectorLabels" -}}
app.kubernetes.io/name: {{ include "service-application.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}