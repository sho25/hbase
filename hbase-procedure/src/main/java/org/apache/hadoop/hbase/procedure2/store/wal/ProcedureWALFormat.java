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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|FSDataOutputStream
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
name|hbase
operator|.
name|io
operator|.
name|util
operator|.
name|StreamUtils
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
name|ProcedureStore
operator|.
name|ProcedureLoader
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
name|procedure2
operator|.
name|util
operator|.
name|ByteSlot
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
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
operator|.
name|ProcedureWALTrailer
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * Helper class that contains the WAL serialization utils.  */
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
specifier|final
class|class
name|ProcedureWALFormat
block|{
specifier|static
specifier|final
name|byte
name|LOG_TYPE_STREAM
init|=
literal|0
decl_stmt|;
specifier|static
specifier|final
name|byte
name|LOG_TYPE_COMPACTED
init|=
literal|1
decl_stmt|;
specifier|static
specifier|final
name|byte
name|LOG_TYPE_MAX_VALID
init|=
literal|1
decl_stmt|;
specifier|static
specifier|final
name|byte
name|HEADER_VERSION
init|=
literal|1
decl_stmt|;
specifier|static
specifier|final
name|byte
name|TRAILER_VERSION
init|=
literal|1
decl_stmt|;
specifier|static
specifier|final
name|long
name|HEADER_MAGIC
init|=
literal|0x31764c4157637250L
decl_stmt|;
specifier|static
specifier|final
name|long
name|TRAILER_MAGIC
init|=
literal|0x50726357414c7631L
decl_stmt|;
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
class|class
name|InvalidWALDataException
extends|extends
name|IOException
block|{
specifier|public
name|InvalidWALDataException
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|super
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
specifier|public
name|InvalidWALDataException
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|super
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
interface|interface
name|Loader
extends|extends
name|ProcedureLoader
block|{
name|void
name|markCorruptedWAL
parameter_list|(
name|ProcedureWALFile
name|log
parameter_list|,
name|IOException
name|e
parameter_list|)
function_decl|;
block|}
specifier|private
name|ProcedureWALFormat
parameter_list|()
block|{}
specifier|public
specifier|static
name|void
name|load
parameter_list|(
specifier|final
name|Iterator
argument_list|<
name|ProcedureWALFile
argument_list|>
name|logs
parameter_list|,
specifier|final
name|ProcedureStoreTracker
name|tracker
parameter_list|,
specifier|final
name|Loader
name|loader
parameter_list|)
throws|throws
name|IOException
block|{
name|ProcedureWALFormatReader
name|reader
init|=
operator|new
name|ProcedureWALFormatReader
argument_list|(
name|tracker
argument_list|)
decl_stmt|;
name|tracker
operator|.
name|setKeepDeletes
argument_list|(
literal|true
argument_list|)
expr_stmt|;
try|try
block|{
while|while
condition|(
name|logs
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|ProcedureWALFile
name|log
init|=
name|logs
operator|.
name|next
argument_list|()
decl_stmt|;
name|log
operator|.
name|open
argument_list|()
expr_stmt|;
try|try
block|{
name|reader
operator|.
name|read
argument_list|(
name|log
argument_list|,
name|loader
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|log
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
name|reader
operator|.
name|finalize
argument_list|(
name|loader
argument_list|)
expr_stmt|;
comment|// The tracker is now updated with all the procedures read from the logs
name|tracker
operator|.
name|setPartialFlag
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|resetUpdates
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|tracker
operator|.
name|setKeepDeletes
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|writeHeader
parameter_list|(
name|OutputStream
name|stream
parameter_list|,
name|ProcedureWALHeader
name|header
parameter_list|)
throws|throws
name|IOException
block|{
name|header
operator|.
name|writeDelimitedTo
argument_list|(
name|stream
argument_list|)
expr_stmt|;
block|}
comment|/*    * +-----------------+    * | END OF WAL DATA |<---+    * +-----------------+     |    * |                 |     |    * |     Tracker     |     |    * |                 |     |    * +-----------------+     |    * |     version     |     |    * +-----------------+     |    * |  TRAILER_MAGIC  |     |    * +-----------------+     |    * |      offset     |-----+    * +-----------------+    */
specifier|public
specifier|static
name|long
name|writeTrailer
parameter_list|(
name|FSDataOutputStream
name|stream
parameter_list|,
name|ProcedureStoreTracker
name|tracker
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|offset
init|=
name|stream
operator|.
name|getPos
argument_list|()
decl_stmt|;
comment|// Write EOF Entry
name|ProcedureWALEntry
operator|.
name|newBuilder
argument_list|()
operator|.
name|setType
argument_list|(
name|ProcedureWALEntry
operator|.
name|Type
operator|.
name|PROCEDURE_WAL_EOF
argument_list|)
operator|.
name|build
argument_list|()
operator|.
name|writeDelimitedTo
argument_list|(
name|stream
argument_list|)
expr_stmt|;
comment|// Write Tracker
name|tracker
operator|.
name|writeTo
argument_list|(
name|stream
argument_list|)
expr_stmt|;
name|stream
operator|.
name|write
argument_list|(
name|TRAILER_VERSION
argument_list|)
expr_stmt|;
name|StreamUtils
operator|.
name|writeLong
argument_list|(
name|stream
argument_list|,
name|TRAILER_MAGIC
argument_list|)
expr_stmt|;
name|StreamUtils
operator|.
name|writeLong
argument_list|(
name|stream
argument_list|,
name|offset
argument_list|)
expr_stmt|;
return|return
name|stream
operator|.
name|getPos
argument_list|()
operator|-
name|offset
return|;
block|}
specifier|public
specifier|static
name|ProcedureWALHeader
name|readHeader
parameter_list|(
name|InputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
name|ProcedureWALHeader
name|header
decl_stmt|;
try|try
block|{
name|header
operator|=
name|ProcedureWALHeader
operator|.
name|parseDelimitedFrom
argument_list|(
name|stream
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InvalidWALDataException
argument_list|(
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|header
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|InvalidWALDataException
argument_list|(
literal|"No data available to read the Header"
argument_list|)
throw|;
block|}
if|if
condition|(
name|header
operator|.
name|getVersion
argument_list|()
operator|<
literal|0
operator|||
name|header
operator|.
name|getVersion
argument_list|()
operator|!=
name|HEADER_VERSION
condition|)
block|{
throw|throw
operator|new
name|InvalidWALDataException
argument_list|(
literal|"Invalid Header version. got "
operator|+
name|header
operator|.
name|getVersion
argument_list|()
operator|+
literal|" expected "
operator|+
name|HEADER_VERSION
argument_list|)
throw|;
block|}
if|if
condition|(
name|header
operator|.
name|getType
argument_list|()
operator|<
literal|0
operator|||
name|header
operator|.
name|getType
argument_list|()
operator|>
name|LOG_TYPE_MAX_VALID
condition|)
block|{
throw|throw
operator|new
name|InvalidWALDataException
argument_list|(
literal|"Invalid header type. got "
operator|+
name|header
operator|.
name|getType
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|header
return|;
block|}
specifier|public
specifier|static
name|ProcedureWALTrailer
name|readTrailer
parameter_list|(
name|FSDataInputStream
name|stream
parameter_list|,
name|long
name|startPos
parameter_list|,
name|long
name|size
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Beginning of the Trailer Jump. 17 = 1 byte version + 8 byte magic + 8 byte offset
name|long
name|trailerPos
init|=
name|size
operator|-
literal|17
decl_stmt|;
if|if
condition|(
name|trailerPos
operator|<
name|startPos
condition|)
block|{
throw|throw
operator|new
name|InvalidWALDataException
argument_list|(
literal|"Missing trailer: size="
operator|+
name|size
operator|+
literal|" startPos="
operator|+
name|startPos
argument_list|)
throw|;
block|}
name|stream
operator|.
name|seek
argument_list|(
name|trailerPos
argument_list|)
expr_stmt|;
name|int
name|version
init|=
name|stream
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|version
operator|!=
name|TRAILER_VERSION
condition|)
block|{
throw|throw
operator|new
name|InvalidWALDataException
argument_list|(
literal|"Invalid Trailer version. got "
operator|+
name|version
operator|+
literal|" expected "
operator|+
name|TRAILER_VERSION
argument_list|)
throw|;
block|}
name|long
name|magic
init|=
name|StreamUtils
operator|.
name|readLong
argument_list|(
name|stream
argument_list|)
decl_stmt|;
if|if
condition|(
name|magic
operator|!=
name|TRAILER_MAGIC
condition|)
block|{
throw|throw
operator|new
name|InvalidWALDataException
argument_list|(
literal|"Invalid Trailer magic. got "
operator|+
name|magic
operator|+
literal|" expected "
operator|+
name|TRAILER_MAGIC
argument_list|)
throw|;
block|}
name|long
name|trailerOffset
init|=
name|StreamUtils
operator|.
name|readLong
argument_list|(
name|stream
argument_list|)
decl_stmt|;
name|stream
operator|.
name|seek
argument_list|(
name|trailerOffset
argument_list|)
expr_stmt|;
name|ProcedureWALEntry
name|entry
init|=
name|readEntry
argument_list|(
name|stream
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|.
name|getType
argument_list|()
operator|!=
name|ProcedureWALEntry
operator|.
name|Type
operator|.
name|PROCEDURE_WAL_EOF
condition|)
block|{
throw|throw
operator|new
name|InvalidWALDataException
argument_list|(
literal|"Invalid Trailer begin"
argument_list|)
throw|;
block|}
name|ProcedureWALTrailer
name|trailer
init|=
name|ProcedureWALTrailer
operator|.
name|newBuilder
argument_list|()
operator|.
name|setVersion
argument_list|(
name|version
argument_list|)
operator|.
name|setTrackerPos
argument_list|(
name|stream
operator|.
name|getPos
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|trailer
return|;
block|}
specifier|public
specifier|static
name|ProcedureWALEntry
name|readEntry
parameter_list|(
name|InputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ProcedureWALEntry
operator|.
name|parseDelimitedFrom
argument_list|(
name|stream
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|writeEntry
parameter_list|(
name|ByteSlot
name|slot
parameter_list|,
name|ProcedureWALEntry
operator|.
name|Type
name|type
parameter_list|,
name|Procedure
name|proc
parameter_list|,
name|Procedure
index|[]
name|subprocs
parameter_list|)
throws|throws
name|IOException
block|{
name|ProcedureWALEntry
operator|.
name|Builder
name|builder
init|=
name|ProcedureWALEntry
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setType
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addProcedure
argument_list|(
name|Procedure
operator|.
name|convert
argument_list|(
name|proc
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|subprocs
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|subprocs
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|builder
operator|.
name|addProcedure
argument_list|(
name|Procedure
operator|.
name|convert
argument_list|(
name|subprocs
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|build
argument_list|()
operator|.
name|writeDelimitedTo
argument_list|(
name|slot
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|writeInsert
parameter_list|(
name|ByteSlot
name|slot
parameter_list|,
name|Procedure
name|proc
parameter_list|)
throws|throws
name|IOException
block|{
name|writeEntry
argument_list|(
name|slot
argument_list|,
name|ProcedureWALEntry
operator|.
name|Type
operator|.
name|PROCEDURE_WAL_INIT
argument_list|,
name|proc
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|writeInsert
parameter_list|(
name|ByteSlot
name|slot
parameter_list|,
name|Procedure
name|proc
parameter_list|,
name|Procedure
index|[]
name|subprocs
parameter_list|)
throws|throws
name|IOException
block|{
name|writeEntry
argument_list|(
name|slot
argument_list|,
name|ProcedureWALEntry
operator|.
name|Type
operator|.
name|PROCEDURE_WAL_INSERT
argument_list|,
name|proc
argument_list|,
name|subprocs
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|writeUpdate
parameter_list|(
name|ByteSlot
name|slot
parameter_list|,
name|Procedure
name|proc
parameter_list|)
throws|throws
name|IOException
block|{
name|writeEntry
argument_list|(
name|slot
argument_list|,
name|ProcedureWALEntry
operator|.
name|Type
operator|.
name|PROCEDURE_WAL_UPDATE
argument_list|,
name|proc
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|writeDelete
parameter_list|(
name|ByteSlot
name|slot
parameter_list|,
name|long
name|procId
parameter_list|)
throws|throws
name|IOException
block|{
name|ProcedureWALEntry
operator|.
name|Builder
name|builder
init|=
name|ProcedureWALEntry
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setType
argument_list|(
name|ProcedureWALEntry
operator|.
name|Type
operator|.
name|PROCEDURE_WAL_DELETE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setProcId
argument_list|(
name|procId
argument_list|)
expr_stmt|;
name|builder
operator|.
name|build
argument_list|()
operator|.
name|writeDelimitedTo
argument_list|(
name|slot
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

