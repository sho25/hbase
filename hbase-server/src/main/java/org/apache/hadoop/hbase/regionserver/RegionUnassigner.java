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
name|HRegionInfo
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

begin_comment
comment|/**  * Used to unssign a region when we hit FNFE.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|RegionUnassigner
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
name|RegionUnassigner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RegionServerServices
name|rsServices
decl_stmt|;
specifier|private
specifier|final
name|HRegionInfo
name|regionInfo
decl_stmt|;
specifier|private
name|boolean
name|unassigning
init|=
literal|false
decl_stmt|;
name|RegionUnassigner
parameter_list|(
name|RegionServerServices
name|rsServices
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
name|this
operator|.
name|rsServices
operator|=
name|rsServices
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
block|}
specifier|synchronized
name|void
name|unassign
parameter_list|()
block|{
if|if
condition|(
name|unassigning
condition|)
block|{
return|return;
block|}
name|unassigning
operator|=
literal|true
expr_stmt|;
operator|new
name|Thread
argument_list|(
literal|"Unassign-"
operator|+
name|regionInfo
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Unassign "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|rsServices
operator|.
name|unassign
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
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
literal|"Unassigned "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
synchronized|synchronized
init|(
name|RegionUnassigner
operator|.
name|this
init|)
block|{
name|unassigning
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
block|}
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

