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
name|chaos
operator|.
name|actions
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
name|Collection
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
name|lang
operator|.
name|math
operator|.
name|RandomUtils
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
name|ServerName
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
name|client
operator|.
name|HTable
import|;
end_import

begin_comment
comment|/** * Action that restarts an HRegionServer holding one of the regions of the table. */
end_comment

begin_class
specifier|public
class|class
name|RestartRsHoldingTableAction
extends|extends
name|RestartActionBaseAction
block|{
specifier|private
specifier|final
name|String
name|tableName
decl_stmt|;
specifier|public
name|RestartRsHoldingTableAction
parameter_list|(
name|long
name|sleepTime
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
name|super
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|perform
parameter_list|()
throws|throws
name|Exception
block|{
name|HTable
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Configuration
name|conf
init|=
name|context
operator|.
name|getHaseIntegrationTestingUtility
argument_list|()
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
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
name|debug
argument_list|(
literal|"Error creating HTable used to get list of region locations."
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|serverNames
init|=
name|table
operator|.
name|getRegionLocations
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
name|ServerName
index|[]
name|nameArray
init|=
name|serverNames
operator|.
name|toArray
argument_list|(
operator|new
name|ServerName
index|[
name|serverNames
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|restartRs
argument_list|(
name|nameArray
index|[
name|RandomUtils
operator|.
name|nextInt
argument_list|(
name|nameArray
operator|.
name|length
argument_list|)
index|]
argument_list|,
name|sleepTime
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

