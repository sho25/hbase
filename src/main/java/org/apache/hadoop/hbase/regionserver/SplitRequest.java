begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|RemoteExceptionHandler
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
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Handles processing region splits. Put in a queue, owned by HRegionServer.  */
end_comment

begin_class
class|class
name|SplitRequest
implements|implements
name|Runnable
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SplitRequest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HRegion
name|parent
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|midKey
decl_stmt|;
specifier|private
specifier|final
name|HRegionServer
name|server
decl_stmt|;
name|SplitRequest
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|byte
index|[]
name|midKey
parameter_list|,
name|HRegionServer
name|hrs
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|hrs
argument_list|)
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|region
expr_stmt|;
name|this
operator|.
name|midKey
operator|=
name|midKey
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|hrs
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
literal|"regionName="
operator|+
name|parent
operator|+
literal|", midKey="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|midKey
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
specifier|final
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|SplitTransaction
name|st
init|=
operator|new
name|SplitTransaction
argument_list|(
name|parent
argument_list|,
name|midKey
argument_list|)
decl_stmt|;
comment|// If prepare does not return true, for some reason -- logged inside in
comment|// the prepare call -- we are not ready to split just now. Just return.
if|if
condition|(
operator|!
name|st
operator|.
name|prepare
argument_list|()
condition|)
return|return;
try|try
block|{
name|st
operator|.
name|execute
argument_list|(
name|this
operator|.
name|server
argument_list|,
name|this
operator|.
name|server
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running rollback of failed split of "
operator|+
name|parent
operator|+
literal|"; "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|st
operator|.
name|rollback
argument_list|(
name|this
operator|.
name|server
argument_list|,
name|this
operator|.
name|server
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Successful rollback of failed split of "
operator|+
name|parent
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|ee
parameter_list|)
block|{
comment|// If failed rollback, kill server to avoid having a hole in table.
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed rollback of failed split of "
operator|+
name|parent
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" -- aborting server"
argument_list|,
name|ee
argument_list|)
expr_stmt|;
name|this
operator|.
name|server
operator|.
name|abort
argument_list|(
literal|"Failed split"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Region split, META updated, and report to master. Parent="
operator|+
name|parent
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|", new regions: "
operator|+
name|st
operator|.
name|getFirstDaughter
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|", "
operator|+
name|st
operator|.
name|getSecondDaughter
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|". Split took "
operator|+
name|StringUtils
operator|.
name|formatTimeDiff
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|startTime
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Split failed "
operator|+
name|this
argument_list|,
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|ex
argument_list|)
argument_list|)
expr_stmt|;
name|server
operator|.
name|checkFileSystem
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

