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
name|regionserver
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|Cell
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
name|KeyValue
operator|.
name|KVComparator
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
name|regionserver
operator|.
name|compactions
operator|.
name|Compactor
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Base class for cell sink that separates the provided cells into multiple files.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|StripeMultiFileWriter
implements|implements
name|Compactor
operator|.
name|CellSink
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
name|StripeMultiFileWriter
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Factory that is used to produce single StoreFile.Writer-s */
specifier|protected
name|WriterFactory
name|writerFactory
decl_stmt|;
specifier|protected
name|KVComparator
name|comparator
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|StoreFile
operator|.
name|Writer
argument_list|>
name|existingWriters
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|boundaries
decl_stmt|;
comment|/** Source scanner that is tracking KV count; may be null if source is not StoreScanner */
specifier|protected
name|StoreScanner
name|sourceScanner
decl_stmt|;
comment|/** Whether to write stripe metadata */
specifier|private
name|boolean
name|doWriteStripeMetadata
init|=
literal|true
decl_stmt|;
specifier|public
interface|interface
name|WriterFactory
block|{
specifier|public
name|StoreFile
operator|.
name|Writer
name|createWriter
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * Initializes multi-writer before usage.    * @param sourceScanner Optional store scanner to obtain the information about read progress.    * @param factory Factory used to produce individual file writers.    * @param comparator Comparator used to compare rows.    */
specifier|public
name|void
name|init
parameter_list|(
name|StoreScanner
name|sourceScanner
parameter_list|,
name|WriterFactory
name|factory
parameter_list|,
name|KVComparator
name|comparator
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|writerFactory
operator|=
name|factory
expr_stmt|;
name|this
operator|.
name|sourceScanner
operator|=
name|sourceScanner
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
block|}
specifier|public
name|void
name|setNoStripeMetadata
parameter_list|()
block|{
name|this
operator|.
name|doWriteStripeMetadata
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|commitWriters
parameter_list|(
name|long
name|maxSeqId
parameter_list|,
name|boolean
name|isMajor
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|this
operator|.
name|existingWriters
operator|!=
literal|null
assert|;
name|commitWritersInternal
argument_list|()
expr_stmt|;
assert|assert
name|this
operator|.
name|boundaries
operator|.
name|size
argument_list|()
operator|==
operator|(
name|this
operator|.
name|existingWriters
operator|.
name|size
argument_list|()
operator|+
literal|1
operator|)
assert|;
name|LOG
operator|.
name|debug
argument_list|(
operator|(
name|this
operator|.
name|doWriteStripeMetadata
condition|?
literal|"W"
else|:
literal|"Not w"
operator|)
operator|+
literal|"riting out metadata for "
operator|+
name|this
operator|.
name|existingWriters
operator|.
name|size
argument_list|()
operator|+
literal|" writers"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|this
operator|.
name|existingWriters
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|StoreFile
operator|.
name|Writer
name|writer
init|=
name|this
operator|.
name|existingWriters
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|writer
operator|==
literal|null
condition|)
continue|continue;
comment|// writer was skipped due to 0 KVs
if|if
condition|(
name|doWriteStripeMetadata
condition|)
block|{
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|StripeStoreFileManager
operator|.
name|STRIPE_START_KEY
argument_list|,
name|this
operator|.
name|boundaries
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|StripeStoreFileManager
operator|.
name|STRIPE_END_KEY
argument_list|,
name|this
operator|.
name|boundaries
operator|.
name|get
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|appendMetadata
argument_list|(
name|maxSeqId
argument_list|,
name|isMajor
argument_list|)
expr_stmt|;
name|paths
operator|.
name|add
argument_list|(
name|writer
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|existingWriters
operator|=
literal|null
expr_stmt|;
return|return
name|paths
return|;
block|}
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|abortWriters
parameter_list|()
block|{
assert|assert
name|this
operator|.
name|existingWriters
operator|!=
literal|null
assert|;
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFile
operator|.
name|Writer
name|writer
range|:
name|this
operator|.
name|existingWriters
control|)
block|{
try|try
block|{
name|paths
operator|.
name|add
argument_list|(
name|writer
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to close the writer after an unfinished compaction."
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|existingWriters
operator|=
literal|null
expr_stmt|;
return|return
name|paths
return|;
block|}
comment|/**    * Subclasses can call this method to make sure the first KV is within multi-writer range.    * @param left The left boundary of the writer.    * @param row The row to check.    * @param rowOffset Offset for row.    * @param rowLength Length for row.    */
specifier|protected
name|void
name|sanityCheckLeft
parameter_list|(
name|byte
index|[]
name|left
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|int
name|rowOffset
parameter_list|,
name|int
name|rowLength
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|StripeStoreFileManager
operator|.
name|OPEN_KEY
operator|!=
name|left
operator|&&
name|comparator
operator|.
name|compareRows
argument_list|(
name|row
argument_list|,
name|rowOffset
argument_list|,
name|rowLength
argument_list|,
name|left
argument_list|,
literal|0
argument_list|,
name|left
operator|.
name|length
argument_list|)
operator|<
literal|0
condition|)
block|{
name|String
name|error
init|=
literal|"The first row is lower than the left boundary of ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|left
argument_list|)
operator|+
literal|"]: ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|row
argument_list|,
name|rowOffset
argument_list|,
name|rowLength
argument_list|)
operator|+
literal|"]"
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|error
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|error
argument_list|)
throw|;
block|}
block|}
comment|/**    * Subclasses can call this method to make sure the last KV is within multi-writer range.    * @param right The right boundary of the writer.    * @param row The row to check.    * @param rowOffset Offset for row.    * @param rowLength Length for row.    */
specifier|protected
name|void
name|sanityCheckRight
parameter_list|(
name|byte
index|[]
name|right
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|int
name|rowOffset
parameter_list|,
name|int
name|rowLength
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|StripeStoreFileManager
operator|.
name|OPEN_KEY
operator|!=
name|right
operator|&&
name|comparator
operator|.
name|compareRows
argument_list|(
name|row
argument_list|,
name|rowOffset
argument_list|,
name|rowLength
argument_list|,
name|right
argument_list|,
literal|0
argument_list|,
name|right
operator|.
name|length
argument_list|)
operator|>=
literal|0
condition|)
block|{
name|String
name|error
init|=
literal|"The last row is higher or equal than the right boundary of ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|right
argument_list|)
operator|+
literal|"]: ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|row
argument_list|,
name|rowOffset
argument_list|,
name|rowLength
argument_list|)
operator|+
literal|"]"
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|error
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|error
argument_list|)
throw|;
block|}
block|}
comment|/**    * Subclasses override this method to be called at the end of a successful sequence of    * append; all appends are processed before this method is called.    */
specifier|protected
specifier|abstract
name|void
name|commitWritersInternal
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * MultiWriter that separates the cells based on fixed row-key boundaries.    * All the KVs between each pair of neighboring boundaries from the list supplied to ctor    * will end up in one file, and separate from all other such pairs.    */
specifier|public
specifier|static
class|class
name|BoundaryMultiWriter
extends|extends
name|StripeMultiFileWriter
block|{
specifier|private
name|StoreFile
operator|.
name|Writer
name|currentWriter
decl_stmt|;
specifier|private
name|byte
index|[]
name|currentWriterEndKey
decl_stmt|;
specifier|private
name|Cell
name|lastCell
decl_stmt|;
specifier|private
name|long
name|cellsInCurrentWriter
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|majorRangeFromIndex
init|=
operator|-
literal|1
decl_stmt|,
name|majorRangeToIndex
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|boolean
name|hasAnyWriter
init|=
literal|false
decl_stmt|;
comment|/**      * @param targetBoundaries The boundaries on which writers/files are separated.      * @param majorRangeFrom Major range is the range for which at least one file should be      *                       written (because all files are included in compaction).      *                       majorRangeFrom is the left boundary.      * @param majorRangeTo The right boundary of majorRange (see majorRangeFrom).      */
specifier|public
name|BoundaryMultiWriter
parameter_list|(
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|targetBoundaries
parameter_list|,
name|byte
index|[]
name|majorRangeFrom
parameter_list|,
name|byte
index|[]
name|majorRangeTo
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|boundaries
operator|=
name|targetBoundaries
expr_stmt|;
name|this
operator|.
name|existingWriters
operator|=
operator|new
name|ArrayList
argument_list|<
name|StoreFile
operator|.
name|Writer
argument_list|>
argument_list|(
name|this
operator|.
name|boundaries
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// "major" range (range for which all files are included) boundaries, if any,
comment|// must match some target boundaries, let's find them.
assert|assert
operator|(
name|majorRangeFrom
operator|==
literal|null
operator|)
operator|==
operator|(
name|majorRangeTo
operator|==
literal|null
operator|)
assert|;
if|if
condition|(
name|majorRangeFrom
operator|!=
literal|null
condition|)
block|{
name|majorRangeFromIndex
operator|=
operator|(
name|majorRangeFrom
operator|==
name|StripeStoreFileManager
operator|.
name|OPEN_KEY
operator|)
condition|?
literal|0
else|:
name|Collections
operator|.
name|binarySearch
argument_list|(
name|this
operator|.
name|boundaries
argument_list|,
name|majorRangeFrom
argument_list|,
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
expr_stmt|;
name|majorRangeToIndex
operator|=
operator|(
name|majorRangeTo
operator|==
name|StripeStoreFileManager
operator|.
name|OPEN_KEY
operator|)
condition|?
name|boundaries
operator|.
name|size
argument_list|()
else|:
name|Collections
operator|.
name|binarySearch
argument_list|(
name|this
operator|.
name|boundaries
argument_list|,
name|majorRangeTo
argument_list|,
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|majorRangeFromIndex
operator|<
literal|0
operator|||
name|this
operator|.
name|majorRangeToIndex
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Major range does not match writer boundaries: ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|majorRangeFrom
argument_list|)
operator|+
literal|"] ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|majorRangeTo
argument_list|)
operator|+
literal|"]; from "
operator|+
name|majorRangeFromIndex
operator|+
literal|" to "
operator|+
name|majorRangeToIndex
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|currentWriter
operator|==
literal|null
operator|&&
name|existingWriters
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// First append ever, do a sanity check.
name|sanityCheckLeft
argument_list|(
name|this
operator|.
name|boundaries
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|prepareWriterFor
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|currentWriter
operator|.
name|append
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|lastCell
operator|=
name|cell
expr_stmt|;
comment|// for the sanity check
operator|++
name|cellsInCurrentWriter
expr_stmt|;
block|}
specifier|private
name|boolean
name|isCellAfterCurrentWriter
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
return|return
operator|(
operator|(
name|currentWriterEndKey
operator|!=
name|StripeStoreFileManager
operator|.
name|OPEN_KEY
operator|)
operator|&&
operator|(
name|comparator
operator|.
name|compareRows
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|currentWriterEndKey
argument_list|,
literal|0
argument_list|,
name|currentWriterEndKey
operator|.
name|length
argument_list|)
operator|>=
literal|0
operator|)
operator|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|commitWritersInternal
parameter_list|()
throws|throws
name|IOException
block|{
name|stopUsingCurrentWriter
argument_list|()
expr_stmt|;
while|while
condition|(
name|existingWriters
operator|.
name|size
argument_list|()
operator|<
name|boundaries
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
name|createEmptyWriter
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|lastCell
operator|!=
literal|null
condition|)
block|{
name|sanityCheckRight
argument_list|(
name|boundaries
operator|.
name|get
argument_list|(
name|boundaries
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|,
name|lastCell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|lastCell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|lastCell
operator|.
name|getRowLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|prepareWriterFor
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|currentWriter
operator|!=
literal|null
operator|&&
operator|!
name|isCellAfterCurrentWriter
argument_list|(
name|cell
argument_list|)
condition|)
return|return;
comment|// Use same writer.
name|stopUsingCurrentWriter
argument_list|()
expr_stmt|;
comment|// See if KV will be past the writer we are about to create; need to add another one.
while|while
condition|(
name|isCellAfterCurrentWriter
argument_list|(
name|cell
argument_list|)
condition|)
block|{
name|checkCanCreateWriter
argument_list|()
expr_stmt|;
name|createEmptyWriter
argument_list|()
expr_stmt|;
block|}
name|checkCanCreateWriter
argument_list|()
expr_stmt|;
name|hasAnyWriter
operator|=
literal|true
expr_stmt|;
name|currentWriter
operator|=
name|writerFactory
operator|.
name|createWriter
argument_list|()
expr_stmt|;
name|existingWriters
operator|.
name|add
argument_list|(
name|currentWriter
argument_list|)
expr_stmt|;
block|}
comment|/**      * Called if there are no cells for some stripe.      * We need to have something in the writer list for this stripe, so that writer-boundary      * list indices correspond to each other. We can insert null in the writer list for that      * purpose, except in the following cases where we actually need a file:      * 1) If we are in range for which we are compacting all the files, we need to create an      * empty file to preserve stripe metadata.      * 2) If we have not produced any file at all for this compactions, and this is the      * last chance (the last stripe), we need to preserve last seqNum (see also HBASE-6059).      */
specifier|private
name|void
name|createEmptyWriter
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|index
init|=
name|existingWriters
operator|.
name|size
argument_list|()
decl_stmt|;
name|boolean
name|isInMajorRange
init|=
operator|(
name|index
operator|>=
name|majorRangeFromIndex
operator|)
operator|&&
operator|(
name|index
operator|<
name|majorRangeToIndex
operator|)
decl_stmt|;
comment|// Stripe boundary count = stripe count + 1, so last stripe index is (#boundaries minus 2)
name|boolean
name|isLastWriter
init|=
operator|!
name|hasAnyWriter
operator|&&
operator|(
name|index
operator|==
operator|(
name|boundaries
operator|.
name|size
argument_list|()
operator|-
literal|2
operator|)
operator|)
decl_stmt|;
name|boolean
name|needEmptyFile
init|=
name|isInMajorRange
operator|||
name|isLastWriter
decl_stmt|;
name|existingWriters
operator|.
name|add
argument_list|(
name|needEmptyFile
condition|?
name|writerFactory
operator|.
name|createWriter
argument_list|()
else|:
literal|null
argument_list|)
expr_stmt|;
name|hasAnyWriter
operator||=
name|needEmptyFile
expr_stmt|;
name|currentWriterEndKey
operator|=
operator|(
name|existingWriters
operator|.
name|size
argument_list|()
operator|+
literal|1
operator|==
name|boundaries
operator|.
name|size
argument_list|()
operator|)
condition|?
literal|null
else|:
name|boundaries
operator|.
name|get
argument_list|(
name|existingWriters
operator|.
name|size
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkCanCreateWriter
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|maxWriterCount
init|=
name|boundaries
operator|.
name|size
argument_list|()
operator|-
literal|1
decl_stmt|;
assert|assert
name|existingWriters
operator|.
name|size
argument_list|()
operator|<=
name|maxWriterCount
assert|;
if|if
condition|(
name|existingWriters
operator|.
name|size
argument_list|()
operator|>=
name|maxWriterCount
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot create any more writers (created "
operator|+
name|existingWriters
operator|.
name|size
argument_list|()
operator|+
literal|" out of "
operator|+
name|maxWriterCount
operator|+
literal|" - row might be out of range of all valid writers"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|stopUsingCurrentWriter
parameter_list|()
block|{
if|if
condition|(
name|currentWriter
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stopping to use a writer after ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|currentWriterEndKey
argument_list|)
operator|+
literal|"] row; wrote out "
operator|+
name|cellsInCurrentWriter
operator|+
literal|" kvs"
argument_list|)
expr_stmt|;
block|}
name|cellsInCurrentWriter
operator|=
literal|0
expr_stmt|;
block|}
name|currentWriter
operator|=
literal|null
expr_stmt|;
name|currentWriterEndKey
operator|=
operator|(
name|existingWriters
operator|.
name|size
argument_list|()
operator|+
literal|1
operator|==
name|boundaries
operator|.
name|size
argument_list|()
operator|)
condition|?
literal|null
else|:
name|boundaries
operator|.
name|get
argument_list|(
name|existingWriters
operator|.
name|size
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * MultiWriter that separates the cells based on target cell number per file and file count.    * New file is started every time the target number of KVs is reached, unless the fixed    * count of writers has already been created (in that case all the remaining KVs go into    * the last writer).    */
specifier|public
specifier|static
class|class
name|SizeMultiWriter
extends|extends
name|StripeMultiFileWriter
block|{
specifier|private
name|int
name|targetCount
decl_stmt|;
specifier|private
name|long
name|targetCells
decl_stmt|;
specifier|private
name|byte
index|[]
name|left
decl_stmt|;
specifier|private
name|byte
index|[]
name|right
decl_stmt|;
specifier|private
name|Cell
name|lastCell
decl_stmt|;
specifier|private
name|StoreFile
operator|.
name|Writer
name|currentWriter
decl_stmt|;
specifier|protected
name|byte
index|[]
name|lastRowInCurrentWriter
init|=
literal|null
decl_stmt|;
specifier|private
name|long
name|cellsInCurrentWriter
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|cellsSeen
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|cellsSeenInPrevious
init|=
literal|0
decl_stmt|;
comment|/**      * @param targetCount The maximum count of writers that can be created.      * @param targetKvs The number of KVs to read from source before starting each new writer.      * @param left The left boundary of the first writer.      * @param right The right boundary of the last writer.      */
specifier|public
name|SizeMultiWriter
parameter_list|(
name|int
name|targetCount
parameter_list|,
name|long
name|targetKvs
parameter_list|,
name|byte
index|[]
name|left
parameter_list|,
name|byte
index|[]
name|right
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|targetCount
operator|=
name|targetCount
expr_stmt|;
name|this
operator|.
name|targetCells
operator|=
name|targetKvs
expr_stmt|;
name|this
operator|.
name|left
operator|=
name|left
expr_stmt|;
name|this
operator|.
name|right
operator|=
name|right
expr_stmt|;
name|int
name|preallocate
init|=
name|Math
operator|.
name|min
argument_list|(
name|this
operator|.
name|targetCount
argument_list|,
literal|64
argument_list|)
decl_stmt|;
name|this
operator|.
name|existingWriters
operator|=
operator|new
name|ArrayList
argument_list|<
name|StoreFile
operator|.
name|Writer
argument_list|>
argument_list|(
name|preallocate
argument_list|)
expr_stmt|;
name|this
operator|.
name|boundaries
operator|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|preallocate
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
comment|// If we are waiting for opportunity to close and we started writing different row,
comment|// discard the writer and stop waiting.
name|boolean
name|doCreateWriter
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|currentWriter
operator|==
literal|null
condition|)
block|{
comment|// First append ever, do a sanity check.
name|sanityCheckLeft
argument_list|(
name|left
argument_list|,
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|)
expr_stmt|;
name|doCreateWriter
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|lastRowInCurrentWriter
operator|!=
literal|null
operator|&&
operator|!
name|comparator
operator|.
name|matchingRows
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|lastRowInCurrentWriter
argument_list|,
literal|0
argument_list|,
name|lastRowInCurrentWriter
operator|.
name|length
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stopping to use a writer after ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|lastRowInCurrentWriter
argument_list|)
operator|+
literal|"] row; wrote out "
operator|+
name|cellsInCurrentWriter
operator|+
literal|" kvs"
argument_list|)
expr_stmt|;
block|}
name|lastRowInCurrentWriter
operator|=
literal|null
expr_stmt|;
name|cellsInCurrentWriter
operator|=
literal|0
expr_stmt|;
name|cellsSeenInPrevious
operator|+=
name|cellsSeen
expr_stmt|;
name|doCreateWriter
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|doCreateWriter
condition|)
block|{
name|byte
index|[]
name|boundary
init|=
name|existingWriters
operator|.
name|isEmpty
argument_list|()
condition|?
name|left
else|:
name|cell
operator|.
name|getRow
argument_list|()
decl_stmt|;
comment|// make a copy
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating new writer starting at ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|boundary
argument_list|)
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|currentWriter
operator|=
name|writerFactory
operator|.
name|createWriter
argument_list|()
expr_stmt|;
name|boundaries
operator|.
name|add
argument_list|(
name|boundary
argument_list|)
expr_stmt|;
name|existingWriters
operator|.
name|add
argument_list|(
name|currentWriter
argument_list|)
expr_stmt|;
block|}
name|currentWriter
operator|.
name|append
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|lastCell
operator|=
name|cell
expr_stmt|;
comment|// for the sanity check
operator|++
name|cellsInCurrentWriter
expr_stmt|;
name|cellsSeen
operator|=
name|cellsInCurrentWriter
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|sourceScanner
operator|!=
literal|null
condition|)
block|{
name|cellsSeen
operator|=
name|Math
operator|.
name|max
argument_list|(
name|cellsSeen
argument_list|,
name|this
operator|.
name|sourceScanner
operator|.
name|getEstimatedNumberOfKvsScanned
argument_list|()
operator|-
name|cellsSeenInPrevious
argument_list|)
expr_stmt|;
block|}
comment|// If we are not already waiting for opportunity to close, start waiting if we can
comment|// create any more writers and if the current one is too big.
if|if
condition|(
name|lastRowInCurrentWriter
operator|==
literal|null
operator|&&
name|existingWriters
operator|.
name|size
argument_list|()
operator|<
name|targetCount
operator|&&
name|cellsSeen
operator|>=
name|targetCells
condition|)
block|{
name|lastRowInCurrentWriter
operator|=
name|cell
operator|.
name|getRow
argument_list|()
expr_stmt|;
comment|// make a copy
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Preparing to start a new writer after ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|lastRowInCurrentWriter
argument_list|)
operator|+
literal|"] row; observed "
operator|+
name|cellsSeen
operator|+
literal|" kvs and wrote out "
operator|+
name|cellsInCurrentWriter
operator|+
literal|" kvs"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|commitWritersInternal
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stopping with "
operator|+
name|cellsInCurrentWriter
operator|+
literal|" kvs in last writer"
operator|+
operator|(
operator|(
name|this
operator|.
name|sourceScanner
operator|==
literal|null
operator|)
condition|?
literal|""
else|:
operator|(
literal|"; observed estimated "
operator|+
name|this
operator|.
name|sourceScanner
operator|.
name|getEstimatedNumberOfKvsScanned
argument_list|()
operator|+
literal|" KVs total"
operator|)
operator|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lastCell
operator|!=
literal|null
condition|)
block|{
name|sanityCheckRight
argument_list|(
name|right
argument_list|,
name|lastCell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|lastCell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|lastCell
operator|.
name|getRowLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// When expired stripes were going to be merged into one, and if no writer was created during
comment|// the compaction, we need to create an empty file to preserve metadata.
if|if
condition|(
name|existingWriters
operator|.
name|isEmpty
argument_list|()
operator|&&
literal|1
operator|==
name|targetCount
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Merge expired stripes into one, create an empty file to preserve metadata."
argument_list|)
expr_stmt|;
block|}
name|boundaries
operator|.
name|add
argument_list|(
name|left
argument_list|)
expr_stmt|;
name|existingWriters
operator|.
name|add
argument_list|(
name|writerFactory
operator|.
name|createWriter
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|boundaries
operator|.
name|add
argument_list|(
name|right
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

