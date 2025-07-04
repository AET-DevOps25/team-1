{{/*
Return chart name
*/}}
{{- define "service-job.name" -}}
service-job
{{- end }}

{{/*
Return canonical full name
*/}}
{{- define "service-job.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "service-job.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end }}

{{/*
Labels used for selector matchLabels
*/}}
{{- define "service-job.selectorLabels" -}}
app.kubernetes.io/name: {{ include "service-job.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}