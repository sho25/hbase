begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * Log cleaner that uses the timestamp of the hlog to determine if it should  * be deleted. By default they are allowed to live for 10 minutes.  */
end_comment

begin_class
specifier|public
class|class
name|TimeToLiveLogCleaner
implements|implements
name|LogCleanerDelegate
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
name|TimeToLiveLogCleaner
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
comment|// Configured time a log can be kept after it was closed
specifier|private
name|long
name|ttl
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|isLogDeletable
parameter_list|(
name|Path
name|filePath
parameter_list|)
block|{
name|long
name|time
init|=
literal|0
decl_stmt|;
name|long
name|currentTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|filePath
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|String
index|[]
name|parts
init|=
name|filePath
operator|.
name|getName
argument_list|()
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
try|try
block|{
name|time
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|parts
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|long
name|life
init|=
name|currentTime
operator|-
name|time
decl_stmt|;
if|if
condition|(
name|life
operator|<
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Found a log newer than current time, "
operator|+
literal|"probably a clock skew"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
name|life
operator|>
name|ttl
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|ttl
operator|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.master.logcleaner.ttl"
argument_list|,
literal|600000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
block|}
end_class

end_unit

