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
name|tool
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
name|nio
operator|.
name|ByteBuffer
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
name|Map
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
name|TableNotFoundException
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
comment|/**  * The tool to let you load the output of {@code HFileOutputFormat} into an existing table  * programmatically. Not thread safe.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|BulkLoadHFiles
block|{
specifier|static
specifier|final
name|String
name|RETRY_ON_IO_EXCEPTION
init|=
literal|"hbase.bulkload.retries.retryOnIOException"
decl_stmt|;
specifier|static
specifier|final
name|String
name|MAX_FILES_PER_REGION_PER_FAMILY
init|=
literal|"hbase.mapreduce.bulkload.max.hfiles.perRegion.perFamily"
decl_stmt|;
specifier|static
specifier|final
name|String
name|ASSIGN_SEQ_IDS
init|=
literal|"hbase.mapreduce.bulkload.assign.sequenceNumbers"
decl_stmt|;
specifier|static
specifier|final
name|String
name|CREATE_TABLE_CONF_KEY
init|=
literal|"create.table"
decl_stmt|;
specifier|static
specifier|final
name|String
name|IGNORE_UNMATCHED_CF_CONF_KEY
init|=
literal|"ignore.unmatched.families"
decl_stmt|;
specifier|static
specifier|final
name|String
name|ALWAYS_COPY_FILES
init|=
literal|"always.copy.files"
decl_stmt|;
comment|/**    * Represents an HFile waiting to be loaded. An queue is used in this class in order to support    * the case where a region has split during the process of the load. When this happens, the HFile    * is split into two physical parts across the new region boundary, and each part is added back    * into the queue. The import process finishes when the queue is empty.    */
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
specifier|static
class|class
name|LoadQueueItem
block|{
specifier|private
specifier|final
name|byte
index|[]
name|family
decl_stmt|;
specifier|private
specifier|final
name|Path
name|hfilePath
decl_stmt|;
specifier|public
name|LoadQueueItem
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|Path
name|hfilePath
parameter_list|)
block|{
name|this
operator|.
name|family
operator|=
name|family
expr_stmt|;
name|this
operator|.
name|hfilePath
operator|=
name|hfilePath
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"family:"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
operator|+
literal|" path:"
operator|+
name|hfilePath
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|byte
index|[]
name|getFamily
parameter_list|()
block|{
return|return
name|family
return|;
block|}
specifier|public
name|Path
name|getFilePath
parameter_list|()
block|{
return|return
name|hfilePath
return|;
block|}
block|}
comment|/**    * Perform a bulk load of the given directory into the given pre-existing table.    * @param tableName the table to load into    * @param family2Files map of family to List of hfiles    * @throws TableNotFoundException if table does not yet exist    */
name|Map
argument_list|<
name|LoadQueueItem
argument_list|,
name|ByteBuffer
argument_list|>
name|bulkLoad
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Path
argument_list|>
argument_list|>
name|family2Files
parameter_list|)
throws|throws
name|TableNotFoundException
throws|,
name|IOException
function_decl|;
comment|/**    * Perform a bulk load of the given directory into the given pre-existing table.    * @param tableName the table to load into    * @param dir the directory that was provided as the output path of a job using    *          {@code HFileOutputFormat}    * @throws TableNotFoundException if table does not yet exist    */
name|Map
argument_list|<
name|LoadQueueItem
argument_list|,
name|ByteBuffer
argument_list|>
name|bulkLoad
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|Path
name|dir
parameter_list|)
throws|throws
name|TableNotFoundException
throws|,
name|IOException
function_decl|;
specifier|static
name|BulkLoadHFiles
name|create
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|BulkLoadHFilesTool
argument_list|(
name|conf
argument_list|)
return|;
block|}
block|}
end_interface

end_unit
