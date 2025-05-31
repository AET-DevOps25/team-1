{{/* vim: set filetype=mustache: */}}

{{- define "aihr.releaseName" -}}
{{- .Release.Name -}}
{{- end -}}

{{- define "aihr.host" -}}
{{- .Values.global.env.host -}}
{{- end -}}

{{- define "aihr.ingress.annotations" -}}
{{- toYaml .Values.global.ingress.annotations -}}
{{- end -}}
