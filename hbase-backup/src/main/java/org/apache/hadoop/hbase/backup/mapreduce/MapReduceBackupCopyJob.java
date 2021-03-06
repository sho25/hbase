begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|backup
operator|.
name|mapreduce
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileSystem
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|backup
operator|.
name|BackupCopyJob
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|backup
operator|.
name|BackupInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|backup
operator|.
name|BackupType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|backup
operator|.
name|impl
operator|.
name|BackupManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|backup
operator|.
name|util
operator|.
name|BackupUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|snapshot
operator|.
name|ExportSnapshot
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|SequenceFile
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Text
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|Cluster
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|Counters
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|Job
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|JobID
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|tools
operator|.
name|CopyListingFileStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|tools
operator|.
name|DistCp
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|tools
operator|.
name|DistCpConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|tools
operator|.
name|DistCpOptions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
operator|.
name|NoNodeException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Map-Reduce implementation of {@link BackupCopyJob}. Basically, there are 2 types of copy  * operation: one is copying from snapshot, which bases on extending ExportSnapshot's function, the  * other is copying for incremental log files, which bases on extending DistCp's function.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MapReduceBackupCopyJob
implements|implements
name|BackupCopyJob
block|{
specifier|public
specifier|static
specifier|final
name|String
name|NUMBER_OF_LEVELS_TO_PRESERVE_KEY
init|=
literal|"num.levels.preserve"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MapReduceBackupCopyJob
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
comment|// Accumulated progress within the whole backup process for the copy operation
specifier|private
name|float
name|progressDone
init|=
literal|0.1f
decl_stmt|;
specifier|private
name|long
name|bytesCopied
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
name|float
name|INIT_PROGRESS
init|=
literal|0.1f
decl_stmt|;
comment|// The percentage of the current copy task within the whole task if multiple time copies are
comment|// needed. The default value is 100%, which means only 1 copy task for the whole.
specifier|private
name|float
name|subTaskPercntgInWholeTask
init|=
literal|1f
decl_stmt|;
specifier|public
name|MapReduceBackupCopyJob
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
comment|/**    * Get the current copy task percentage within the whole task if multiple copies are needed.    * @return the current copy task percentage    */
specifier|public
name|float
name|getSubTaskPercntgInWholeTask
parameter_list|()
block|{
return|return
name|subTaskPercntgInWholeTask
return|;
block|}
comment|/**    * Set the current copy task percentage within the whole task if multiple copies are needed. Must    * be called before calling    * {@link #copy(BackupInfo, BackupManager, Configuration, BackupType, String[])}    * @param subTaskPercntgInWholeTask The percentage of the copy subtask    */
specifier|public
name|void
name|setSubTaskPercntgInWholeTask
parameter_list|(
name|float
name|subTaskPercntgInWholeTask
parameter_list|)
block|{
name|this
operator|.
name|subTaskPercntgInWholeTask
operator|=
name|subTaskPercntgInWholeTask
expr_stmt|;
block|}
specifier|static
class|class
name|SnapshotCopy
extends|extends
name|ExportSnapshot
block|{
specifier|private
name|BackupInfo
name|backupInfo
decl_stmt|;
specifier|private
name|TableName
name|table
decl_stmt|;
specifier|public
name|SnapshotCopy
parameter_list|(
name|BackupInfo
name|backupInfo
parameter_list|,
name|TableName
name|table
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|backupInfo
operator|=
name|backupInfo
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
specifier|public
name|TableName
name|getTable
parameter_list|()
block|{
return|return
name|this
operator|.
name|table
return|;
block|}
specifier|public
name|BackupInfo
name|getBackupInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|backupInfo
return|;
block|}
block|}
comment|/**    * Update the ongoing backup with new progress.    * @param backupInfo backup info    * @param newProgress progress    * @param bytesCopied bytes copied    * @throws NoNodeException exception    */
specifier|static
name|void
name|updateProgress
parameter_list|(
name|BackupInfo
name|backupInfo
parameter_list|,
name|BackupManager
name|backupManager
parameter_list|,
name|int
name|newProgress
parameter_list|,
name|long
name|bytesCopied
parameter_list|)
throws|throws
name|IOException
block|{
comment|// compose the new backup progress data, using fake number for now
name|String
name|backupProgressData
init|=
name|newProgress
operator|+
literal|"%"
decl_stmt|;
name|backupInfo
operator|.
name|setProgress
argument_list|(
name|newProgress
argument_list|)
expr_stmt|;
name|backupManager
operator|.
name|updateBackupInfo
argument_list|(
name|backupInfo
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Backup progress data \""
operator|+
name|backupProgressData
operator|+
literal|"\" has been updated to backup system table for "
operator|+
name|backupInfo
operator|.
name|getBackupId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Extends DistCp for progress updating to backup system table    * during backup. Using DistCpV2 (MAPREDUCE-2765).    * Simply extend it and override execute() method to get the    * Job reference for progress updating.    * Only the argument "src1, [src2, [...]] dst" is supported,    * no more DistCp options.    */
class|class
name|BackupDistCp
extends|extends
name|DistCp
block|{
specifier|private
name|BackupInfo
name|backupInfo
decl_stmt|;
specifier|private
name|BackupManager
name|backupManager
decl_stmt|;
specifier|public
name|BackupDistCp
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|DistCpOptions
name|options
parameter_list|,
name|BackupInfo
name|backupInfo
parameter_list|,
name|BackupManager
name|backupManager
parameter_list|)
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|options
argument_list|)
expr_stmt|;
name|this
operator|.
name|backupInfo
operator|=
name|backupInfo
expr_stmt|;
name|this
operator|.
name|backupManager
operator|=
name|backupManager
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Job
name|execute
parameter_list|()
throws|throws
name|Exception
block|{
comment|// reflection preparation for private methods and fields
name|Class
argument_list|<
name|?
argument_list|>
name|classDistCp
init|=
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|tools
operator|.
name|DistCp
operator|.
name|class
decl_stmt|;
name|Method
name|methodCleanup
init|=
name|classDistCp
operator|.
name|getDeclaredMethod
argument_list|(
literal|"cleanup"
argument_list|)
decl_stmt|;
name|Field
name|fieldInputOptions
init|=
name|getInputOptionsField
argument_list|(
name|classDistCp
argument_list|)
decl_stmt|;
name|Field
name|fieldSubmitted
init|=
name|classDistCp
operator|.
name|getDeclaredField
argument_list|(
literal|"submitted"
argument_list|)
decl_stmt|;
name|methodCleanup
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|fieldInputOptions
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|fieldSubmitted
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// execute() logic starts here
assert|assert
name|fieldInputOptions
operator|.
name|get
argument_list|(
name|this
argument_list|)
operator|!=
literal|null
assert|;
name|Job
name|job
init|=
literal|null
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|srcs
init|=
name|getSourcePaths
argument_list|(
name|fieldInputOptions
argument_list|)
decl_stmt|;
name|long
name|totalSrcLgth
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Path
name|aSrc
range|:
name|srcs
control|)
block|{
name|totalSrcLgth
operator|+=
name|BackupUtils
operator|.
name|getFilesLength
argument_list|(
name|aSrc
operator|.
name|getFileSystem
argument_list|(
name|super
operator|.
name|getConf
argument_list|()
argument_list|)
argument_list|,
name|aSrc
argument_list|)
expr_stmt|;
block|}
comment|// Async call
name|job
operator|=
name|super
operator|.
name|execute
argument_list|()
expr_stmt|;
comment|// Update the copy progress to system table every 0.5s if progress value changed
name|int
name|progressReportFreq
init|=
name|MapReduceBackupCopyJob
operator|.
name|this
operator|.
name|getConf
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.backup.progressreport.frequency"
argument_list|,
literal|500
argument_list|)
decl_stmt|;
name|float
name|lastProgress
init|=
name|progressDone
decl_stmt|;
while|while
condition|(
operator|!
name|job
operator|.
name|isComplete
argument_list|()
condition|)
block|{
name|float
name|newProgress
init|=
name|progressDone
operator|+
name|job
operator|.
name|mapProgress
argument_list|()
operator|*
name|subTaskPercntgInWholeTask
operator|*
operator|(
literal|1
operator|-
name|INIT_PROGRESS
operator|)
decl_stmt|;
if|if
condition|(
name|newProgress
operator|>
name|lastProgress
condition|)
block|{
name|BigDecimal
name|progressData
init|=
operator|new
name|BigDecimal
argument_list|(
name|newProgress
operator|*
literal|100
argument_list|)
operator|.
name|setScale
argument_list|(
literal|1
argument_list|,
name|BigDecimal
operator|.
name|ROUND_HALF_UP
argument_list|)
decl_stmt|;
name|String
name|newProgressStr
init|=
name|progressData
operator|+
literal|"%"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Progress: "
operator|+
name|newProgressStr
argument_list|)
expr_stmt|;
name|updateProgress
argument_list|(
name|backupInfo
argument_list|,
name|backupManager
argument_list|,
name|progressData
operator|.
name|intValue
argument_list|()
argument_list|,
name|bytesCopied
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Backup progress data updated to backup system table: \"Progress: "
operator|+
name|newProgressStr
operator|+
literal|".\""
argument_list|)
expr_stmt|;
name|lastProgress
operator|=
name|newProgress
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|progressReportFreq
argument_list|)
expr_stmt|;
block|}
comment|// update the progress data after copy job complete
name|float
name|newProgress
init|=
name|progressDone
operator|+
name|job
operator|.
name|mapProgress
argument_list|()
operator|*
name|subTaskPercntgInWholeTask
operator|*
operator|(
literal|1
operator|-
name|INIT_PROGRESS
operator|)
decl_stmt|;
name|BigDecimal
name|progressData
init|=
operator|new
name|BigDecimal
argument_list|(
name|newProgress
operator|*
literal|100
argument_list|)
operator|.
name|setScale
argument_list|(
literal|1
argument_list|,
name|BigDecimal
operator|.
name|ROUND_HALF_UP
argument_list|)
decl_stmt|;
name|String
name|newProgressStr
init|=
name|progressData
operator|+
literal|"%"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Progress: "
operator|+
name|newProgressStr
operator|+
literal|" subTask: "
operator|+
name|subTaskPercntgInWholeTask
operator|+
literal|" mapProgress: "
operator|+
name|job
operator|.
name|mapProgress
argument_list|()
argument_list|)
expr_stmt|;
comment|// accumulate the overall backup progress
name|progressDone
operator|=
name|newProgress
expr_stmt|;
name|bytesCopied
operator|+=
name|totalSrcLgth
expr_stmt|;
name|updateProgress
argument_list|(
name|backupInfo
argument_list|,
name|backupManager
argument_list|,
name|progressData
operator|.
name|intValue
argument_list|()
argument_list|,
name|bytesCopied
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Backup progress data updated to backup system table: \"Progress: "
operator|+
name|newProgressStr
operator|+
literal|" - "
operator|+
name|bytesCopied
operator|+
literal|" bytes copied.\""
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|t
operator|.
name|toString
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
name|String
name|jobID
init|=
name|job
operator|.
name|getJobID
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|DistCpConstants
operator|.
name|CONF_LABEL_DISTCP_JOB_ID
argument_list|,
name|jobID
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"DistCp job-id: "
operator|+
name|jobID
operator|+
literal|" completed: "
operator|+
name|job
operator|.
name|isComplete
argument_list|()
operator|+
literal|" "
operator|+
name|job
operator|.
name|isSuccessful
argument_list|()
argument_list|)
expr_stmt|;
name|Counters
name|ctrs
init|=
name|job
operator|.
name|getCounters
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|Objects
operator|.
name|toString
argument_list|(
name|ctrs
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|job
operator|.
name|isComplete
argument_list|()
operator|&&
operator|!
name|job
operator|.
name|isSuccessful
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"DistCp job-id: "
operator|+
name|jobID
operator|+
literal|" failed"
argument_list|)
throw|;
block|}
return|return
name|job
return|;
block|}
specifier|private
name|Field
name|getInputOptionsField
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|classDistCp
parameter_list|)
throws|throws
name|IOException
block|{
name|Field
name|f
init|=
literal|null
decl_stmt|;
try|try
block|{
name|f
operator|=
name|classDistCp
operator|.
name|getDeclaredField
argument_list|(
literal|"inputOptions"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Haddop 3
try|try
block|{
name|f
operator|=
name|classDistCp
operator|.
name|getDeclaredField
argument_list|(
literal|"context"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
decl||
name|SecurityException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e1
argument_list|)
throw|;
block|}
block|}
return|return
name|f
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|List
argument_list|<
name|Path
argument_list|>
name|getSourcePaths
parameter_list|(
name|Field
name|fieldInputOptions
parameter_list|)
throws|throws
name|IOException
block|{
name|Object
name|options
decl_stmt|;
try|try
block|{
name|options
operator|=
name|fieldInputOptions
operator|.
name|get
argument_list|(
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
name|options
operator|instanceof
name|DistCpOptions
condition|)
block|{
return|return
operator|(
operator|(
name|DistCpOptions
operator|)
name|options
operator|)
operator|.
name|getSourcePaths
argument_list|()
return|;
block|}
else|else
block|{
comment|// Hadoop 3
name|Class
argument_list|<
name|?
argument_list|>
name|classContext
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.tools.DistCpContext"
argument_list|)
decl_stmt|;
name|Method
name|methodGetSourcePaths
init|=
name|classContext
operator|.
name|getDeclaredMethod
argument_list|(
literal|"getSourcePaths"
argument_list|)
decl_stmt|;
name|methodGetSourcePaths
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
operator|(
name|List
argument_list|<
name|Path
argument_list|>
operator|)
name|methodGetSourcePaths
operator|.
name|invoke
argument_list|(
name|options
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
decl||
name|IllegalAccessException
decl||
name|ClassNotFoundException
decl||
name|NoSuchMethodException
decl||
name|SecurityException
decl||
name|InvocationTargetException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|Path
name|createInputFileListing
parameter_list|(
name|Job
name|job
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|NUMBER_OF_LEVELS_TO_PRESERVE_KEY
argument_list|)
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|createInputFileListing
argument_list|(
name|job
argument_list|)
return|;
block|}
name|long
name|totalBytesExpected
init|=
literal|0
decl_stmt|;
name|int
name|totalRecords
init|=
literal|0
decl_stmt|;
name|Path
name|fileListingPath
init|=
name|getFileListingPath
argument_list|()
decl_stmt|;
try|try
init|(
name|SequenceFile
operator|.
name|Writer
name|writer
init|=
name|getWriter
argument_list|(
name|fileListingPath
argument_list|)
init|)
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|srcFiles
init|=
name|getSourceFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|srcFiles
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|fileListingPath
return|;
block|}
name|totalRecords
operator|=
name|srcFiles
operator|.
name|size
argument_list|()
expr_stmt|;
name|FileSystem
name|fs
init|=
name|srcFiles
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|srcFiles
control|)
block|{
name|FileStatus
name|fst
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|totalBytesExpected
operator|+=
name|fst
operator|.
name|getLen
argument_list|()
expr_stmt|;
name|Text
name|key
init|=
name|getKey
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|key
argument_list|,
operator|new
name|CopyListingFileStatus
argument_list|(
name|fst
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// update jobs configuration
name|Configuration
name|cfg
init|=
name|job
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|cfg
operator|.
name|setLong
argument_list|(
name|DistCpConstants
operator|.
name|CONF_LABEL_TOTAL_BYTES_TO_BE_COPIED
argument_list|,
name|totalBytesExpected
argument_list|)
expr_stmt|;
name|cfg
operator|.
name|set
argument_list|(
name|DistCpConstants
operator|.
name|CONF_LABEL_LISTING_FILE_PATH
argument_list|,
name|fileListingPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|cfg
operator|.
name|setLong
argument_list|(
name|DistCpConstants
operator|.
name|CONF_LABEL_TOTAL_NUMBER_OF_RECORDS
argument_list|,
name|totalRecords
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
decl||
name|SecurityException
decl||
name|IllegalArgumentException
decl||
name|IllegalAccessException
decl||
name|NoSuchMethodException
decl||
name|ClassNotFoundException
decl||
name|InvocationTargetException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|fileListingPath
return|;
block|}
specifier|private
name|Text
name|getKey
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|int
name|level
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|NUMBER_OF_LEVELS_TO_PRESERVE_KEY
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
name|String
name|relPath
init|=
literal|""
decl_stmt|;
while|while
condition|(
name|count
operator|++
operator|<
name|level
condition|)
block|{
name|relPath
operator|=
name|Path
operator|.
name|SEPARATOR
operator|+
name|path
operator|.
name|getName
argument_list|()
operator|+
name|relPath
expr_stmt|;
name|path
operator|=
name|path
operator|.
name|getParent
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|Text
argument_list|(
name|relPath
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|Path
argument_list|>
name|getSourceFiles
parameter_list|()
throws|throws
name|NoSuchFieldException
throws|,
name|SecurityException
throws|,
name|IllegalArgumentException
throws|,
name|IllegalAccessException
throws|,
name|NoSuchMethodException
throws|,
name|ClassNotFoundException
throws|,
name|InvocationTargetException
throws|,
name|IOException
block|{
name|Field
name|options
init|=
literal|null
decl_stmt|;
try|try
block|{
name|options
operator|=
name|DistCp
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"inputOptions"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
decl||
name|SecurityException
name|e
parameter_list|)
block|{
name|options
operator|=
name|DistCp
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"context"
argument_list|)
expr_stmt|;
block|}
name|options
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|getSourcePaths
argument_list|(
name|options
argument_list|)
return|;
block|}
specifier|private
name|SequenceFile
operator|.
name|Writer
name|getWriter
parameter_list|(
name|Path
name|pathToListFile
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|pathToListFile
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|pathToListFile
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
name|SequenceFile
operator|.
name|createWriter
argument_list|(
name|conf
argument_list|,
name|SequenceFile
operator|.
name|Writer
operator|.
name|file
argument_list|(
name|pathToListFile
argument_list|)
argument_list|,
name|SequenceFile
operator|.
name|Writer
operator|.
name|keyClass
argument_list|(
name|Text
operator|.
name|class
argument_list|)
argument_list|,
name|SequenceFile
operator|.
name|Writer
operator|.
name|valueClass
argument_list|(
name|CopyListingFileStatus
operator|.
name|class
argument_list|)
argument_list|,
name|SequenceFile
operator|.
name|Writer
operator|.
name|compression
argument_list|(
name|SequenceFile
operator|.
name|CompressionType
operator|.
name|NONE
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|/**    * Do backup copy based on different types.    * @param context The backup info    * @param conf The hadoop configuration    * @param copyType The backup copy type    * @param options Options for customized ExportSnapshot or DistCp    * @throws Exception exception    */
annotation|@
name|Override
specifier|public
name|int
name|copy
parameter_list|(
name|BackupInfo
name|context
parameter_list|,
name|BackupManager
name|backupManager
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|BackupType
name|copyType
parameter_list|,
name|String
index|[]
name|options
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|res
init|=
literal|0
decl_stmt|;
try|try
block|{
if|if
condition|(
name|copyType
operator|==
name|BackupType
operator|.
name|FULL
condition|)
block|{
name|SnapshotCopy
name|snapshotCp
init|=
operator|new
name|SnapshotCopy
argument_list|(
name|context
argument_list|,
name|context
operator|.
name|getTableBySnapshot
argument_list|(
name|options
index|[
literal|1
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Doing SNAPSHOT_COPY"
argument_list|)
expr_stmt|;
comment|// Make a new instance of conf to be used by the snapshot copy class.
name|snapshotCp
operator|.
name|setConf
argument_list|(
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|res
operator|=
name|snapshotCp
operator|.
name|run
argument_list|(
name|options
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|copyType
operator|==
name|BackupType
operator|.
name|INCREMENTAL
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Doing COPY_TYPE_DISTCP"
argument_list|)
expr_stmt|;
name|setSubTaskPercntgInWholeTask
argument_list|(
literal|1f
argument_list|)
expr_stmt|;
name|BackupDistCp
name|distcp
init|=
operator|new
name|BackupDistCp
argument_list|(
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
argument_list|,
literal|null
argument_list|,
name|context
argument_list|,
name|backupManager
argument_list|)
decl_stmt|;
comment|// Handle a special case where the source file is a single file.
comment|// In this case, distcp will not create the target dir. It just take the
comment|// target as a file name and copy source file to the target (as a file name).
comment|// We need to create the target dir before run distcp.
name|LOG
operator|.
name|debug
argument_list|(
literal|"DistCp options: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|options
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|dest
init|=
operator|new
name|Path
argument_list|(
name|options
index|[
name|options
operator|.
name|length
operator|-
literal|1
index|]
argument_list|)
decl_stmt|;
name|String
index|[]
name|newOptions
init|=
operator|new
name|String
index|[
name|options
operator|.
name|length
operator|+
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|options
argument_list|,
literal|0
argument_list|,
name|newOptions
argument_list|,
literal|1
argument_list|,
name|options
operator|.
name|length
argument_list|)
expr_stmt|;
name|newOptions
index|[
literal|0
index|]
operator|=
literal|"-async"
expr_stmt|;
comment|// run DisCp in async mode
name|FileSystem
name|destfs
init|=
name|dest
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|destfs
operator|.
name|exists
argument_list|(
name|dest
argument_list|)
condition|)
block|{
name|destfs
operator|.
name|mkdirs
argument_list|(
name|dest
argument_list|)
expr_stmt|;
block|}
name|res
operator|=
name|distcp
operator|.
name|run
argument_list|(
name|newOptions
argument_list|)
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|cancel
parameter_list|(
name|String
name|jobId
parameter_list|)
throws|throws
name|IOException
block|{
name|JobID
name|id
init|=
name|JobID
operator|.
name|forName
argument_list|(
name|jobId
argument_list|)
decl_stmt|;
name|Cluster
name|cluster
init|=
operator|new
name|Cluster
argument_list|(
name|this
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Job
name|job
init|=
name|cluster
operator|.
name|getJob
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|job
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"No job found for "
operator|+
name|id
argument_list|)
expr_stmt|;
comment|// should we throw exception
return|return;
block|}
if|if
condition|(
name|job
operator|.
name|isComplete
argument_list|()
operator|||
name|job
operator|.
name|isRetired
argument_list|()
condition|)
block|{
return|return;
block|}
name|job
operator|.
name|killJob
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Killed copy job "
operator|+
name|id
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

