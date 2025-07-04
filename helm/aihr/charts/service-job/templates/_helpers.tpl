{{- define "service-job.fullname" -}}
{{- printf "%s-job" .Release.Name -}}
{{- end -}}

{{- define "service-job.selectorLabels" -}}
app.kubernetes.io/name: {{ include "service-job.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}