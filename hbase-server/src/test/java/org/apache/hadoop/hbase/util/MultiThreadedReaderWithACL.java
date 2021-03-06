begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|util
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
name|security
operator|.
name|PrivilegedExceptionAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|client
operator|.
name|Get
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
name|Result
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
name|Table
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
name|HBaseKerberosUtils
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
name|test
operator|.
name|LoadTestDataGenerator
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
name|security
operator|.
name|UserGroupInformation
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
comment|/**  * A MultiThreadReader that helps to work with ACL  */
end_comment

begin_class
specifier|public
class|class
name|MultiThreadedReaderWithACL
extends|extends
name|MultiThreadedReader
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
name|MultiThreadedReaderWithACL
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COMMA
init|=
literal|","
decl_stmt|;
comment|/**    * Maps user with Table instance. Because the table instance has to be created    * per user inorder to work in that user's context    */
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Table
argument_list|>
name|userVsTable
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|User
argument_list|>
name|users
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|String
index|[]
name|userNames
decl_stmt|;
specifier|public
name|MultiThreadedReaderWithACL
parameter_list|(
name|LoadTestDataGenerator
name|dataGen
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|double
name|verifyPercent
parameter_list|,
name|String
name|userNames
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|tableName
argument_list|,
name|verifyPercent
argument_list|)
expr_stmt|;
name|this
operator|.
name|userNames
operator|=
name|userNames
operator|.
name|split
argument_list|(
name|COMMA
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addReaderThreads
parameter_list|(
name|int
name|numThreads
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numThreads
condition|;
operator|++
name|i
control|)
block|{
name|HBaseReaderThread
name|reader
init|=
operator|new
name|HBaseReaderThreadWithACL
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|readers
operator|.
name|add
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
class|class
name|HBaseReaderThreadWithACL
extends|extends
name|HBaseReaderThread
block|{
specifier|public
name|HBaseReaderThreadWithACL
parameter_list|(
name|int
name|readerId
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|readerId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Table
name|createTable
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|closeTable
parameter_list|()
block|{
for|for
control|(
name|Table
name|table
range|:
name|userVsTable
operator|.
name|values
argument_list|()
control|)
block|{
try|try
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error while closing the table "
operator|+
name|table
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|queryKey
parameter_list|(
specifier|final
name|Get
name|get
parameter_list|,
specifier|final
name|boolean
name|verify
parameter_list|,
specifier|final
name|long
name|keyToRead
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|String
name|rowKey
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|get
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
comment|// read the data
specifier|final
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
name|action
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|Table
name|localTable
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Result
name|result
init|=
literal|null
decl_stmt|;
name|int
name|specialPermCellInsertionFactor
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|dataGenerator
operator|.
name|getArgs
argument_list|()
index|[
literal|2
index|]
argument_list|)
decl_stmt|;
name|int
name|mod
init|=
operator|(
operator|(
name|int
operator|)
name|keyToRead
operator|%
name|userNames
operator|.
name|length
operator|)
decl_stmt|;
if|if
condition|(
name|userVsTable
operator|.
name|get
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|)
operator|==
literal|null
condition|)
block|{
name|localTable
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|userVsTable
operator|.
name|put
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|,
name|localTable
argument_list|)
expr_stmt|;
name|result
operator|=
name|localTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|localTable
operator|=
name|userVsTable
operator|.
name|get
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|)
expr_stmt|;
name|result
operator|=
name|localTable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
name|boolean
name|isNullExpected
init|=
operator|(
operator|(
operator|(
operator|(
name|int
operator|)
name|keyToRead
operator|%
name|specialPermCellInsertionFactor
operator|)
operator|)
operator|==
literal|0
operator|)
decl_stmt|;
name|long
name|end
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|verifyResultsAndUpdateMetrics
argument_list|(
name|verify
argument_list|,
name|get
argument_list|,
name|end
operator|-
name|start
argument_list|,
name|result
argument_list|,
name|localTable
argument_list|,
name|isNullExpected
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|recordFailure
argument_list|(
name|keyToRead
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
if|if
condition|(
name|userNames
operator|!=
literal|null
operator|&&
name|userNames
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|int
name|mod
init|=
operator|(
operator|(
name|int
operator|)
name|keyToRead
operator|%
name|userNames
operator|.
name|length
operator|)
decl_stmt|;
name|User
name|user
decl_stmt|;
name|UserGroupInformation
name|realUserUgi
decl_stmt|;
if|if
condition|(
operator|!
name|users
operator|.
name|containsKey
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|)
condition|)
block|{
if|if
condition|(
name|User
operator|.
name|isHBaseSecurityEnabled
argument_list|(
name|conf
argument_list|)
condition|)
block|{
name|realUserUgi
operator|=
name|HBaseKerberosUtils
operator|.
name|loginAndReturnUGI
argument_list|(
name|conf
argument_list|,
name|userNames
index|[
name|mod
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|realUserUgi
operator|=
name|UserGroupInformation
operator|.
name|createRemoteUser
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|)
expr_stmt|;
block|}
name|user
operator|=
name|User
operator|.
name|create
argument_list|(
name|realUserUgi
argument_list|)
expr_stmt|;
name|users
operator|.
name|put
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|,
name|user
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|user
operator|=
name|users
operator|.
name|get
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|user
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|recordFailure
argument_list|(
name|keyToRead
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|recordFailure
parameter_list|(
specifier|final
name|long
name|keyToRead
parameter_list|)
block|{
name|numReadFailures
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"["
operator|+
name|readerId
operator|+
literal|"] FAILED read, key = "
operator|+
operator|(
name|keyToRead
operator|+
literal|""
operator|)
operator|+
literal|", "
operator|+
literal|"time from start: "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTimeMs
operator|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

