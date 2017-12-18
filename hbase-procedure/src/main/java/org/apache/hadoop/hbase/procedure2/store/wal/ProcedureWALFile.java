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
name|procedure2
operator|.
name|store
operator|.
name|wal
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FSDataInputStream
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|procedure2
operator|.
name|store
operator|.
name|ProcedureStoreTracker
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
operator|.
name|ProcedureWALHeader
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
operator|.
name|ProcedureWALTrailer
import|;
end_import

begin_comment
comment|/**  * Describes a WAL File  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ProcedureWALFile
implements|implements
name|Comparable
argument_list|<
name|ProcedureWALFile
argument_list|>
block|{
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
name|ProcedureWALFile
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ProcedureWALHeader
name|header
decl_stmt|;
specifier|private
name|FSDataInputStream
name|stream
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Path
name|logFile
decl_stmt|;
specifier|private
name|long
name|startPos
decl_stmt|;
specifier|private
name|long
name|minProcId
decl_stmt|;
specifier|private
name|long
name|maxProcId
decl_stmt|;
specifier|private
name|long
name|logSize
decl_stmt|;
specifier|private
name|long
name|timestamp
decl_stmt|;
specifier|public
name|ProcedureStoreTracker
name|getTracker
parameter_list|()
block|{
return|return
name|tracker
return|;
block|}
specifier|private
specifier|final
name|ProcedureStoreTracker
name|tracker
init|=
operator|new
name|ProcedureStoreTracker
argument_list|()
decl_stmt|;
specifier|public
name|ProcedureWALFile
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|FileStatus
name|logStatus
parameter_list|)
block|{
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|logFile
operator|=
name|logStatus
operator|.
name|getPath
argument_list|()
expr_stmt|;
name|this
operator|.
name|logSize
operator|=
name|logStatus
operator|.
name|getLen
argument_list|()
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|logStatus
operator|.
name|getModificationTime
argument_list|()
expr_stmt|;
name|tracker
operator|.
name|setPartialFlag
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ProcedureWALFile
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|logFile
parameter_list|,
name|ProcedureWALHeader
name|header
parameter_list|,
name|long
name|startPos
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|header
operator|=
name|header
expr_stmt|;
name|this
operator|.
name|logFile
operator|=
name|logFile
expr_stmt|;
name|this
operator|.
name|startPos
operator|=
name|startPos
expr_stmt|;
name|this
operator|.
name|logSize
operator|=
name|startPos
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|tracker
operator|.
name|setPartialFlag
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|open
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|stream
operator|==
literal|null
condition|)
block|{
name|stream
operator|=
name|fs
operator|.
name|open
argument_list|(
name|logFile
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|header
operator|==
literal|null
condition|)
block|{
name|header
operator|=
name|ProcedureWALFormat
operator|.
name|readHeader
argument_list|(
name|stream
argument_list|)
expr_stmt|;
name|startPos
operator|=
name|stream
operator|.
name|getPos
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|stream
operator|.
name|seek
argument_list|(
name|startPos
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|ProcedureWALTrailer
name|readTrailer
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|ProcedureWALFormat
operator|.
name|readTrailer
argument_list|(
name|stream
argument_list|,
name|startPos
argument_list|,
name|logSize
argument_list|)
return|;
block|}
finally|finally
block|{
name|stream
operator|.
name|seek
argument_list|(
name|startPos
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|readTracker
parameter_list|()
throws|throws
name|IOException
block|{
name|ProcedureWALTrailer
name|trailer
init|=
name|readTrailer
argument_list|()
decl_stmt|;
try|try
block|{
name|stream
operator|.
name|seek
argument_list|(
name|trailer
operator|.
name|getTrackerPos
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|ProcedureProtos
operator|.
name|ProcedureStoreTracker
name|trackerProtoBuf
init|=
name|ProcedureProtos
operator|.
name|ProcedureStoreTracker
operator|.
name|parseDelimitedFrom
argument_list|(
name|stream
argument_list|)
decl_stmt|;
name|tracker
operator|.
name|resetToProto
argument_list|(
name|trackerProtoBuf
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|stream
operator|.
name|seek
argument_list|(
name|startPos
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|updateLocalTracker
parameter_list|(
name|ProcedureStoreTracker
name|tracker
parameter_list|)
block|{
name|this
operator|.
name|tracker
operator|.
name|resetTo
argument_list|(
name|tracker
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|stream
operator|==
literal|null
condition|)
return|return;
try|try
block|{
name|stream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"unable to close the wal file: "
operator|+
name|logFile
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|stream
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
name|FSDataInputStream
name|getStream
parameter_list|()
block|{
return|return
name|stream
return|;
block|}
specifier|public
name|ProcedureWALHeader
name|getHeader
parameter_list|()
block|{
return|return
name|header
return|;
block|}
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
specifier|public
name|boolean
name|isCompacted
parameter_list|()
block|{
return|return
name|header
operator|.
name|getType
argument_list|()
operator|==
name|ProcedureWALFormat
operator|.
name|LOG_TYPE_COMPACTED
return|;
block|}
specifier|public
name|long
name|getLogId
parameter_list|()
block|{
return|return
name|header
operator|.
name|getLogId
argument_list|()
return|;
block|}
specifier|public
name|long
name|getSize
parameter_list|()
block|{
return|return
name|logSize
return|;
block|}
comment|/**    * Used to update in-progress log sizes. the FileStatus will report 0 otherwise.    */
name|void
name|addToSize
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|this
operator|.
name|logSize
operator|+=
name|size
expr_stmt|;
block|}
specifier|public
name|void
name|removeFile
parameter_list|(
specifier|final
name|Path
name|walArchiveDir
parameter_list|)
throws|throws
name|IOException
block|{
name|close
argument_list|()
expr_stmt|;
name|boolean
name|archived
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|walArchiveDir
operator|!=
literal|null
condition|)
block|{
name|Path
name|archivedFile
init|=
operator|new
name|Path
argument_list|(
name|walArchiveDir
argument_list|,
name|logFile
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"ARCHIVED (TODO: FILES ARE NOT PURGED FROM ARCHIVE!) "
operator|+
name|logFile
operator|+
literal|" to "
operator|+
name|archivedFile
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|logFile
argument_list|,
name|archivedFile
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed archive of "
operator|+
name|logFile
operator|+
literal|", deleting"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|archived
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|archived
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|logFile
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed delete of "
operator|+
name|logFile
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|setProcIds
parameter_list|(
name|long
name|minId
parameter_list|,
name|long
name|maxId
parameter_list|)
block|{
name|this
operator|.
name|minProcId
operator|=
name|minId
expr_stmt|;
name|this
operator|.
name|maxProcId
operator|=
name|maxId
expr_stmt|;
block|}
specifier|public
name|long
name|getMinProcId
parameter_list|()
block|{
return|return
name|minProcId
return|;
block|}
specifier|public
name|long
name|getMaxProcId
parameter_list|()
block|{
return|return
name|maxProcId
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
specifier|final
name|ProcedureWALFile
name|other
parameter_list|)
block|{
name|long
name|diff
init|=
name|header
operator|.
name|getLogId
argument_list|()
operator|-
name|other
operator|.
name|header
operator|.
name|getLogId
argument_list|()
decl_stmt|;
return|return
operator|(
name|diff
operator|<
literal|0
operator|)
condition|?
operator|-
literal|1
else|:
operator|(
name|diff
operator|>
literal|0
operator|)
condition|?
literal|1
else|:
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|ProcedureWALFile
operator|)
condition|)
return|return
literal|false
return|;
return|return
name|compareTo
argument_list|(
operator|(
name|ProcedureWALFile
operator|)
name|o
argument_list|)
operator|==
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|logFile
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|logFile
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

