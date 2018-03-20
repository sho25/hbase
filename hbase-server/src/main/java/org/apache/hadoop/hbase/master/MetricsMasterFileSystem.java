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
name|master
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
name|hbase
operator|.
name|CompatibilitySingletonFactory
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsMasterFileSystem
block|{
specifier|private
specifier|final
name|MetricsMasterFileSystemSource
name|source
decl_stmt|;
specifier|public
name|MetricsMasterFileSystem
parameter_list|()
block|{
name|source
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsMasterFileSystemSource
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|/**    * Record a single instance of a split    * @param time time that the split took    * @param size length of original WALs that were split    */
specifier|public
specifier|synchronized
name|void
name|addSplit
parameter_list|(
name|long
name|time
parameter_list|,
name|long
name|size
parameter_list|)
block|{
name|source
operator|.
name|updateSplitTime
argument_list|(
name|time
argument_list|)
expr_stmt|;
name|source
operator|.
name|updateSplitSize
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
comment|/**    * Record a single instance of a split    * @param time time that the split took    * @param size length of original WALs that were split    */
specifier|public
specifier|synchronized
name|void
name|addMetaWALSplit
parameter_list|(
name|long
name|time
parameter_list|,
name|long
name|size
parameter_list|)
block|{
name|source
operator|.
name|updateMetaWALSplitTime
argument_list|(
name|time
argument_list|)
expr_stmt|;
name|source
operator|.
name|updateMetaWALSplitSize
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

