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
name|ArrayList
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
name|hbase
operator|.
name|regionserver
operator|.
name|StoreConfigInformation
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
name|StoreFile
import|;
end_import

begin_comment
comment|/**  * Test Policy to compact everything every time.  */
end_comment

begin_class
specifier|public
class|class
name|EverythingPolicy
extends|extends
name|RatioBasedCompactionPolicy
block|{
comment|/**    * Constructor.    *    * @param conf            The Conf.    * @param storeConfigInfo Info about the store.    */
specifier|public
name|EverythingPolicy
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|StoreConfigInformation
name|storeConfigInfo
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|storeConfigInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
specifier|final
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|applyCompactionPolicy
parameter_list|(
specifier|final
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|candidates
parameter_list|,
specifier|final
name|boolean
name|mayUseOffPeak
parameter_list|,
specifier|final
name|boolean
name|mayBeStuck
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|candidates
operator|.
name|size
argument_list|()
operator|<
name|comConf
operator|.
name|getMinFilesToCompact
argument_list|()
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|(
literal|0
argument_list|)
return|;
block|}
return|return
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|(
name|candidates
argument_list|)
return|;
block|}
block|}
end_class

end_unit

