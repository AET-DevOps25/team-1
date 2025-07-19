export type SortField = 'name' | 'job' | 'status' | 'date' | 'score' | 'finalScore' | 'hrDecision';
export type SortDirection = 'asc' | 'desc';
export type ViewMode = 'applications' | 'jobs';

export interface Assessment {
  assessmentId: string;
  resumeScore: number;
  chatScore: number;
  finalScore: number;
  aiResumeAnalysis: string;
  aiChatSummary: string;
  aiRecommendStatus: string;
  resume_score?: number;
  chat_score?: number;
  final_score?: number;
  ai_resume_analysis?: string;
  ai_chat_summary?: string;
  ai_recommend_status?: string;
}

export interface Application {
  applicationId: string;
  jobId: string;
  candidateId: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  candidate: {
    candidateId: string;
    fullName: string;
    email: string;
  };
  job: {
    jobId: string;
    title: string;
    description: string;
    status: string;
  };
  assessment?: Assessment;
  hrDecision?: string;
  hrComments?: string;
  application_id?: string;
  candidate_name?: string;
  email?: string;
  job_title?: string;
  submission_timestamp?: string;
  final_score?: number | null;
  avatarColor?: string;
  hr_decision?: string;
  hr_comment?: string;
}

export interface Job {
  jobId: string;
  title: string;
  description: string;
  requirements: string;
  status: 'DRAFT' | 'OPEN' | 'CLOSED';
  createdAt: string;
}

export interface JobForm {
  jobId?: string;
  title: string;
  description: string;
  requirements: string;
  status: 'DRAFT' | 'OPEN';
}

export interface HrForm {
  fullName: string;
  email: string;
  password: string;
}

export interface ValidationError {
  field: string;
  message: string;
}

export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
  errors?: ValidationError[];
}

export interface StatusOption {
  value: string;
  label: string;
  icon?: string;
  description?: string;
}

export type SortValue = string | number | Date;