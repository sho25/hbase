begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
package|;
end_package

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
name|util
operator|.
name|Bytes
import|;
end_import

begin_class
specifier|public
class|class
name|SecureBulkLoadUtil
block|{
specifier|private
specifier|final
specifier|static
name|String
name|BULKLOAD_STAGING_DIR
init|=
literal|"hbase.bulkload.staging.dir"
decl_stmt|;
comment|/**    * This returns the staging path for a given column family.    * This is needed for clean recovery and called reflectively in LoadIncrementalHFiles    */
specifier|public
specifier|static
name|Path
name|getStagingPath
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|bulkToken
parameter_list|,
name|byte
index|[]
name|family
parameter_list|)
block|{
name|Path
name|stageP
init|=
operator|new
name|Path
argument_list|(
name|getBaseStagingDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|bulkToken
argument_list|)
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|stageP
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Path
name|getBaseStagingDir
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|BULKLOAD_STAGING_DIR
argument_list|,
literal|"/tmp/hbase-staging"
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

