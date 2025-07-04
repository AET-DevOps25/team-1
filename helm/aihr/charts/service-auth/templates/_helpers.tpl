{{/*
Return chart name
*/}}
{{- define "service-auth.name" -}}
service-auth
{{- end }}

{{/*
Return canonical full name
*/}}
{{- define "service-auth.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "service-auth.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end }}

{{/*
Labels used for selector matchLabels
*/}}
{{- define "service-auth.selectorLabels" -}}
app.kubernetes.io/name: {{ include "service-auth.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}