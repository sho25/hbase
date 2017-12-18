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
name|java
operator|.
name|util
operator|.
name|OptionalLong
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
name|DateTieredMultiFileWriter
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
name|StoreUtils
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
comment|/**  * This compactor will generate StoreFile for different time ranges.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DateTieredCompactor
extends|extends
name|AbstractMultiOutputCompactor
argument_list|<
name|DateTieredMultiFileWriter
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
name|DateTieredCompactor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|DateTieredCompactor
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
name|boolean
name|needEmptyFile
parameter_list|(
name|CompactionRequestImpl
name|request
parameter_list|)
block|{
comment|// if we are going to compact the last N files, then we need to emit an empty file to retain the
comment|// maxSeqId if we haven't written out anything.
name|OptionalLong
name|maxSeqId
init|=
name|StoreUtils
operator|.
name|getMaxSequenceIdInList
argument_list|(
name|request
operator|.
name|getFiles
argument_list|()
argument_list|)
decl_stmt|;
name|OptionalLong
name|storeMaxSeqId
init|=
name|store
operator|.
name|getMaxSequenceId
argument_list|()
decl_stmt|;
return|return
name|maxSeqId
operator|.
name|isPresent
argument_list|()
operator|&&
name|storeMaxSeqId
operator|.
name|isPresent
argument_list|()
operator|&&
name|maxSeqId
operator|.
name|getAsLong
argument_list|()
operator|==
name|storeMaxSeqId
operator|.
name|getAsLong
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|compact
parameter_list|(
specifier|final
name|CompactionRequestImpl
name|request
parameter_list|,
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|lowerBoundaries
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
name|lowerBoundaries
operator|.
name|size
argument_list|()
operator|+
literal|"windows, lower boundaries: "
operator|+
name|lowerBoundaries
argument_list|)
expr_stmt|;
block|}
return|return
name|compact
argument_list|(
name|request
argument_list|,
name|defaultScannerFactory
argument_list|,
operator|new
name|CellSinkFactory
argument_list|<
name|DateTieredMultiFileWriter
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|DateTieredMultiFileWriter
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
name|DateTieredMultiFileWriter
name|writer
init|=
operator|new
name|DateTieredMultiFileWriter
argument_list|(
name|lowerBoundaries
argument_list|,
name|needEmptyFile
argument_list|(
name|request
argument_list|)
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
name|DateTieredMultiFileWriter
name|writer
parameter_list|,
name|FileDetails
name|fd
parameter_list|,
name|CompactionRequestImpl
name|request
parameter_list|)
throws|throws
name|IOException
block|{
return|return
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
name|isAllFiles
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

