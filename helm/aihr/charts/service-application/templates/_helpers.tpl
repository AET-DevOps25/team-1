{{/*
返回基础名称，用于 label/selector
*/}}
{{- define "service-application.name" -}}
service-application
{{- end }}

{{/*
生成 release 级唯一全名
*/}}
{{- define "service-application.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "service-application.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end }}

{{/*
SelectorLabels：保证 Service / Deployment / ServiceMonitor 使用同一组标签
*/}}
{{- define "service-application.selectorLabels" -}}
app.kubernetes.io/name: {{ include "service-application.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}