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
operator|.
name|compactions
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
name|regionserver
operator|.
name|HStore
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
name|InternalScanner
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
name|ScanType
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
name|StoreFileScanner
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
name|StripeMultiFileWriter
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
name|throttle
operator|.
name|ThroughputController
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
name|security
operator|.
name|User
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

begin_comment
comment|/**  * This is the placeholder for stripe compactor. The implementation, as well as the proper javadoc,  * will be added in HBASE-7967.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|StripeCompactor
extends|extends
name|AbstractMultiOutputCompactor
argument_list|<
name|StripeMultiFileWriter
argument_list|>
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
name|StripeCompactor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|StripeCompactor
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HStore
name|store
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|store
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|final
class|class
name|StripeInternalScannerFactory
implements|implements
name|InternalScannerFactory
block|{
specifier|private
specifier|final
name|byte
index|[]
name|majorRangeFromRow
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|majorRangeToRow
decl_stmt|;
specifier|public
name|StripeInternalScannerFactory
parameter_list|(
name|byte
index|[]
name|majorRangeFromRow
parameter_list|,
name|byte
index|[]
name|majorRangeToRow
parameter_list|)
block|{
name|this
operator|.
name|majorRangeFromRow
operator|=
name|majorRangeFromRow
expr_stmt|;
name|this
operator|.
name|majorRangeToRow
operator|=
name|majorRangeToRow
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ScanType
name|getScanType
parameter_list|(
name|CompactionRequest
name|request
parameter_list|)
block|{
comment|// If majorRangeFromRow and majorRangeToRow are not null, then we will not use the return
comment|// value to create InternalScanner. See the createScanner method below. The return value is
comment|// also used when calling coprocessor hooks.
return|return
name|ScanType
operator|.
name|COMPACT_RETAIN_DELETES
return|;
block|}
annotation|@
name|Override
specifier|public
name|InternalScanner
name|createScanner
parameter_list|(
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|scanners
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|FileDetails
name|fd
parameter_list|,
name|long
name|smallestReadPoint
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|(
name|majorRangeFromRow
operator|==
literal|null
operator|)
condition|?
name|StripeCompactor
operator|.
name|this
operator|.
name|createScanner
argument_list|(
name|store
argument_list|,
name|scanners
argument_list|,
name|scanType
argument_list|,
name|smallestReadPoint
argument_list|,
name|fd
operator|.
name|earliestPutTs
argument_list|)
else|:
name|StripeCompactor
operator|.
name|this
operator|.
name|createScanner
argument_list|(
name|store
argument_list|,
name|scanners
argument_list|,
name|smallestReadPoint
argument_list|,
name|fd
operator|.
name|earliestPutTs
argument_list|,
name|majorRangeFromRow
argument_list|,
name|majorRangeToRow
argument_list|)
return|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|compact
parameter_list|(
name|CompactionRequest
name|request
parameter_list|,
specifier|final
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|targetBoundaries
parameter_list|,
specifier|final
name|byte
index|[]
name|majorRangeFromRow
parameter_list|,
specifier|final
name|byte
index|[]
name|majorRangeToRow
parameter_list|,
name|ThroughputController
name|throughputController
parameter_list|,
name|User
name|user
parameter_list|)
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
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Executing compaction with "
operator|+
name|targetBoundaries
operator|.
name|size
argument_list|()
operator|+
literal|" boundaries:"
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|tb
range|:
name|targetBoundaries
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" ["
argument_list|)
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|tb
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|compact
argument_list|(
name|request
argument_list|,
operator|new
name|StripeInternalScannerFactory
argument_list|(
name|majorRangeFromRow
argument_list|,
name|majorRangeToRow
argument_list|)
argument_list|,
operator|new
name|CellSinkFactory
argument_list|<
name|StripeMultiFileWriter
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StripeMultiFileWriter
name|createWriter
parameter_list|(
name|InternalScanner
name|scanner
parameter_list|,
name|FileDetails
name|fd
parameter_list|,
name|boolean
name|shouldDropBehind
parameter_list|)
throws|throws
name|IOException
block|{
name|StripeMultiFileWriter
name|writer
init|=
operator|new
name|StripeMultiFileWriter
operator|.
name|BoundaryMultiWriter
argument_list|(
name|store
operator|.
name|getComparator
argument_list|()
argument_list|,
name|targetBoundaries
argument_list|,
name|majorRangeFromRow
argument_list|,
name|majorRangeToRow
argument_list|)
decl_stmt|;
name|initMultiWriter
argument_list|(
name|writer
argument_list|,
name|scanner
argument_list|,
name|fd
argument_list|,
name|shouldDropBehind
argument_list|)
expr_stmt|;
return|return
name|writer
return|;
block|}
block|}
argument_list|,
name|throughputController
argument_list|,
name|user
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|compact
parameter_list|(
name|CompactionRequest
name|request
parameter_list|,
specifier|final
name|int
name|targetCount
parameter_list|,
specifier|final
name|long
name|targetSize
parameter_list|,
specifier|final
name|byte
index|[]
name|left
parameter_list|,
specifier|final
name|byte
index|[]
name|right
parameter_list|,
name|byte
index|[]
name|majorRangeFromRow
parameter_list|,
name|byte
index|[]
name|majorRangeToRow
parameter_list|,
name|ThroughputController
name|throughputController
parameter_list|,
name|User
name|user
parameter_list|)
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
literal|"Executing compaction with "
operator|+
name|targetSize
operator|+
literal|" target file size, no more than "
operator|+
name|targetCount
operator|+
literal|" files, in ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|left
argument_list|)
operator|+
literal|"] ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|right
argument_list|)
operator|+
literal|"] range"
argument_list|)
expr_stmt|;
block|}
return|return
name|compact
argument_list|(
name|request
argument_list|,
operator|new
name|StripeInternalScannerFactory
argument_list|(
name|majorRangeFromRow
argument_list|,
name|majorRangeToRow
argument_list|)
argument_list|,
operator|new
name|CellSinkFactory
argument_list|<
name|StripeMultiFileWriter
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StripeMultiFileWriter
name|createWriter
parameter_list|(
name|InternalScanner
name|scanner
parameter_list|,
name|FileDetails
name|fd
parameter_list|,
name|boolean
name|shouldDropBehind
parameter_list|)
throws|throws
name|IOException
block|{
name|StripeMultiFileWriter
name|writer
init|=
operator|new
name|StripeMultiFileWriter
operator|.
name|SizeMultiWriter
argument_list|(
name|store
operator|.
name|getComparator
argument_list|()
argument_list|,
name|targetCount
argument_list|,
name|targetSize
argument_list|,
name|left
argument_list|,
name|right
argument_list|)
decl_stmt|;
name|initMultiWriter
argument_list|(
name|writer
argument_list|,
name|scanner
argument_list|,
name|fd
argument_list|,
name|shouldDropBehind
argument_list|)
expr_stmt|;
return|return
name|writer
return|;
block|}
block|}
argument_list|,
name|throughputController
argument_list|,
name|user
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|Path
argument_list|>
name|commitWriter
parameter_list|(
name|StripeMultiFileWriter
name|writer
parameter_list|,
name|FileDetails
name|fd
parameter_list|,
name|CompactionRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|newFiles
init|=
name|writer
operator|.
name|commitWriters
argument_list|(
name|fd
operator|.
name|maxSeqId
argument_list|,
name|request
operator|.
name|isMajor
argument_list|()
argument_list|)
decl_stmt|;
assert|assert
operator|!
name|newFiles
operator|.
name|isEmpty
argument_list|()
operator|:
literal|"Should have produced an empty file to preserve metadata."
assert|;
return|return
name|newFiles
return|;
block|}
block|}
end_class

end_unit

