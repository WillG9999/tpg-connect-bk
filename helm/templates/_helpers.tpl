{{/*
Expand the name of the chart.
*/}}
{{- define "connect-backend.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "connect-backend.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "connect-backend.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "connect-backend.labels" -}}
helm.sh/chart: {{ include "connect-backend.chart" . }}
{{ include "connect-backend.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
environment: {{ .Values.environment.name }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "connect-backend.selectorLabels" -}}
app.kubernetes.io/name: {{ include "connect-backend.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
environment: {{ .Values.environment.name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "connect-backend.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "connect-backend.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Create image name with tag
*/}}
{{- define "connect-backend.image" -}}
{{- printf "%s:%s" .Values.image.repository (.Values.image.tag | default .Chart.AppVersion) }}
{{- end }}

{{/*
Environment-specific service name
*/}}
{{- define "connect-backend.serviceName" -}}
{{- if eq .Values.environment.name "prod" }}
{{- printf "%s-service" .Values.app.name }}
{{- else }}
{{- printf "%s-service-%s" .Values.app.name .Values.environment.name }}
{{- end }}
{{- end }}

{{/*
Environment-specific deployment name
*/}}
{{- define "connect-backend.deploymentName" -}}
{{- if eq .Values.environment.name "prod" }}
{{- .Values.app.name }}
{{- else }}
{{- printf "%s-%s" .Values.app.name .Values.environment.name }}
{{- end }}
{{- end }}