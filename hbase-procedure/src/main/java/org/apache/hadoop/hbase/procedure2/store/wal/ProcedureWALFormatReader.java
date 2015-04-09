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
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|classification
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
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
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
name|hbase
operator|.
name|procedure2
operator|.
name|Procedure
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
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
operator|.
name|ProcedureWALEntry
import|;
end_import

begin_comment
comment|/**  * Helper class that loads the procedures stored in a WAL  */
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
name|ProcedureWALFormatReader
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ProcedureWALFormatReader
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ProcedureStoreTracker
name|tracker
decl_stmt|;
comment|//private final long compactionLogId;
specifier|private
specifier|final
name|Map
argument_list|<
name|Long
argument_list|,
name|Procedure
argument_list|>
name|procedures
init|=
operator|new
name|HashMap
argument_list|<
name|Long
argument_list|,
name|Procedure
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Long
argument_list|,
name|ProcedureProtos
operator|.
name|Procedure
argument_list|>
name|localProcedures
init|=
operator|new
name|HashMap
argument_list|<
name|Long
argument_list|,
name|ProcedureProtos
operator|.
name|Procedure
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|long
name|maxProcId
init|=
literal|0
decl_stmt|;
specifier|public
name|ProcedureWALFormatReader
parameter_list|(
specifier|final
name|ProcedureStoreTracker
name|tracker
parameter_list|)
block|{
name|this
operator|.
name|tracker
operator|=
name|tracker
expr_stmt|;
block|}
specifier|public
name|void
name|read
parameter_list|(
name|ProcedureWALFile
name|log
parameter_list|,
name|ProcedureWALFormat
operator|.
name|Loader
name|loader
parameter_list|)
throws|throws
name|IOException
block|{
name|FSDataInputStream
name|stream
init|=
name|log
operator|.
name|getStream
argument_list|()
decl_stmt|;
try|try
block|{
name|boolean
name|hasMore
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|hasMore
condition|)
block|{
name|ProcedureWALEntry
name|entry
init|=
name|ProcedureWALFormat
operator|.
name|readEntry
argument_list|(
name|stream
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"nothing left to decode. exiting with missing EOF"
argument_list|)
expr_stmt|;
name|hasMore
operator|=
literal|false
expr_stmt|;
break|break;
block|}
switch|switch
condition|(
name|entry
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|INIT
case|:
name|readInitEntry
argument_list|(
name|entry
argument_list|)
expr_stmt|;
break|break;
case|case
name|INSERT
case|:
name|readInsertEntry
argument_list|(
name|entry
argument_list|)
expr_stmt|;
break|break;
case|case
name|UPDATE
case|:
case|case
name|COMPACT
case|:
name|readUpdateEntry
argument_list|(
name|entry
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE
case|:
name|readDeleteEntry
argument_list|(
name|entry
argument_list|)
expr_stmt|;
break|break;
case|case
name|EOF
case|:
name|hasMore
operator|=
literal|false
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|CorruptedWALProcedureStoreException
argument_list|(
literal|"Invalid entry: "
operator|+
name|entry
argument_list|)
throw|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"got an exception while reading the procedure WAL: "
operator|+
name|log
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|loader
operator|.
name|markCorruptedWAL
argument_list|(
name|log
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|localProcedures
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No active entry found in state log "
operator|+
name|log
operator|+
literal|". removing it"
argument_list|)
expr_stmt|;
name|loader
operator|.
name|removeLog
argument_list|(
name|log
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|ProcedureProtos
operator|.
name|Procedure
argument_list|>
argument_list|>
name|itd
init|=
name|localProcedures
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|itd
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|ProcedureProtos
operator|.
name|Procedure
argument_list|>
name|entry
init|=
name|itd
operator|.
name|next
argument_list|()
decl_stmt|;
name|itd
operator|.
name|remove
argument_list|()
expr_stmt|;
comment|// Deserialize the procedure
name|Procedure
name|proc
init|=
name|Procedure
operator|.
name|convert
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|procedures
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|proc
argument_list|)
expr_stmt|;
block|}
comment|// TODO: Some procedure may be already runnables (see readInitEntry())
comment|//       (we can also check the "update map" in the log trackers)
block|}
block|}
specifier|public
name|Iterator
argument_list|<
name|Procedure
argument_list|>
name|getProcedures
parameter_list|()
block|{
return|return
name|procedures
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
specifier|private
name|void
name|loadEntries
parameter_list|(
specifier|final
name|ProcedureWALEntry
name|entry
parameter_list|)
block|{
for|for
control|(
name|ProcedureProtos
operator|.
name|Procedure
name|proc
range|:
name|entry
operator|.
name|getProcedureList
argument_list|()
control|)
block|{
name|maxProcId
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxProcId
argument_list|,
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|isRequired
argument_list|(
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"read "
operator|+
name|entry
operator|.
name|getType
argument_list|()
operator|+
literal|" entry "
operator|+
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|localProcedures
operator|.
name|put
argument_list|(
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|,
name|proc
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|setDeleted
argument_list|(
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|readInitEntry
parameter_list|(
specifier|final
name|ProcedureWALEntry
name|entry
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|entry
operator|.
name|getProcedureCount
argument_list|()
operator|==
literal|1
operator|:
literal|"Expected only one procedure"
assert|;
comment|// TODO: Make it runnable, before reading other files
name|loadEntries
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|readInsertEntry
parameter_list|(
specifier|final
name|ProcedureWALEntry
name|entry
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|entry
operator|.
name|getProcedureCount
argument_list|()
operator|>=
literal|1
operator|:
literal|"Expected one or more procedures"
assert|;
name|loadEntries
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|readUpdateEntry
parameter_list|(
specifier|final
name|ProcedureWALEntry
name|entry
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|entry
operator|.
name|getProcedureCount
argument_list|()
operator|==
literal|1
operator|:
literal|"Expected only one procedure"
assert|;
name|loadEntries
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|readDeleteEntry
parameter_list|(
specifier|final
name|ProcedureWALEntry
name|entry
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|entry
operator|.
name|getProcedureCount
argument_list|()
operator|==
literal|0
operator|:
literal|"Expected no procedures"
assert|;
assert|assert
name|entry
operator|.
name|hasProcId
argument_list|()
operator|:
literal|"expected ProcID"
assert|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"read delete entry "
operator|+
name|entry
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|maxProcId
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxProcId
argument_list|,
name|entry
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|localProcedures
operator|.
name|remove
argument_list|(
name|entry
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|setDeleted
argument_list|(
name|entry
operator|.
name|getProcId
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|isDeleted
parameter_list|(
specifier|final
name|long
name|procId
parameter_list|)
block|{
return|return
name|tracker
operator|.
name|isDeleted
argument_list|(
name|procId
argument_list|)
operator|==
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|YES
return|;
block|}
specifier|private
name|boolean
name|isRequired
parameter_list|(
specifier|final
name|long
name|procId
parameter_list|)
block|{
return|return
operator|!
name|isDeleted
argument_list|(
name|procId
argument_list|)
operator|&&
operator|!
name|procedures
operator|.
name|containsKey
argument_list|(
name|procId
argument_list|)
return|;
block|}
block|}
end_class

end_unit

