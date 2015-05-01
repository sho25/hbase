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
name|security
operator|.
name|visibility
package|;
end_package

begin_import
import|import static
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
name|visibility
operator|.
name|VisibilityConstants
operator|.
name|LABELS_TABLE_NAME
import|;
end_import

begin_import
import|import static
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
name|visibility
operator|.
name|VisibilityUtils
operator|.
name|SYSTEM_LABEL
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|HConstants
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
name|Connection
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
name|ConnectionFactory
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
name|ResultScanner
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
name|Scan
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|RegionActionResult
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|NameBytesPair
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
name|protobuf
operator|.
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|ListLabelsResponse
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
name|protobuf
operator|.
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|VisibilityLabelsResponse
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
name|testclassification
operator|.
name|MediumTests
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
name|testclassification
operator|.
name|SecurityTests
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
name|hbase
operator|.
name|util
operator|.
name|JVMClusterUtil
operator|.
name|RegionServerThread
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
name|Threads
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|SecurityTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestVisibilityLabelsWithDefaultVisLabelService
extends|extends
name|TestVisibilityLabels
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
name|TestVisibilityLabelsWithDefaultVisLabelService
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// setup configuration
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|DISTRIBUTED_LOG_REPLAY_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.online.schema.update.enable"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|VisibilityTestUtil
operator|.
name|enableVisiblityLabels
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
name|VisibilityUtils
operator|.
name|VISIBILITY_LABEL_GENERATOR_CLASS
argument_list|,
name|SimpleScanLabelGenerator
operator|.
name|class
argument_list|,
name|ScanLabelGenerator
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.superuser"
argument_list|,
literal|"admin"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|SUPERUSER
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"admin"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"supergroup"
block|}
argument_list|)
expr_stmt|;
name|USER1
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"user1"
argument_list|,
operator|new
name|String
index|[]
block|{}
argument_list|)
expr_stmt|;
comment|// Wait for the labels table to become available
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|LABELS_TABLE_NAME
operator|.
name|getName
argument_list|()
argument_list|,
literal|50000
argument_list|)
expr_stmt|;
name|addLabels
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAddLabels
parameter_list|()
throws|throws
name|Throwable
block|{
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
name|action
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
argument_list|()
block|{
specifier|public
name|VisibilityLabelsResponse
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|labels
init|=
block|{
literal|"L1"
block|,
name|SECRET
block|,
literal|"L2"
block|,
literal|"invalid~"
block|,
literal|"L3"
block|}
decl_stmt|;
name|VisibilityLabelsResponse
name|response
init|=
literal|null
decl_stmt|;
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|)
block|{
name|response
operator|=
name|VisibilityClient
operator|.
name|addLabels
argument_list|(
name|conn
argument_list|,
name|labels
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Should not have thrown exception"
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|RegionActionResult
argument_list|>
name|resultList
init|=
name|response
operator|.
name|getResultList
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|resultList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|resultList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"org.apache.hadoop.hbase.DoNotRetryIOException"
argument_list|,
name|resultList
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|resultList
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
operator|.
name|contains
argument_list|(
literal|"org.apache.hadoop.hbase.security.visibility.LabelAlreadyExistsException: "
operator|+
literal|"Label 'secret' already exists"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|resultList
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|resultList
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|resultList
operator|.
name|get
argument_list|(
literal|4
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60
operator|*
literal|1000
argument_list|)
specifier|public
name|void
name|testAddVisibilityLabelsOnRSRestart
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|RegionServerThread
argument_list|>
name|regionServerThreads
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionServerThread
name|rsThread
range|:
name|regionServerThreads
control|)
block|{
name|rsThread
operator|.
name|getRegionServer
argument_list|()
operator|.
name|abort
argument_list|(
literal|"Aborting "
argument_list|)
expr_stmt|;
block|}
comment|// Start one new RS
name|RegionServerThread
name|rs
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|startRegionServer
argument_list|()
decl_stmt|;
name|waitForLabelsRegionAvailability
argument_list|(
name|rs
operator|.
name|getRegionServer
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|AtomicBoolean
name|vcInitialized
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
do|do
block|{
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
name|action
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
argument_list|()
block|{
specifier|public
name|VisibilityLabelsResponse
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|labels
init|=
block|{
name|SECRET
block|,
name|CONFIDENTIAL
block|,
name|PRIVATE
block|,
literal|"ABC"
block|,
literal|"XYZ"
block|}
decl_stmt|;
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|)
block|{
name|VisibilityLabelsResponse
name|resp
init|=
name|VisibilityClient
operator|.
name|addLabels
argument_list|(
name|conn
argument_list|,
name|labels
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RegionActionResult
argument_list|>
name|results
init|=
name|resp
operator|.
name|getResultList
argument_list|()
decl_stmt|;
if|if
condition|(
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|hasException
argument_list|()
condition|)
block|{
name|NameBytesPair
name|pair
init|=
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getException
argument_list|()
decl_stmt|;
name|Throwable
name|t
init|=
name|ProtobufUtil
operator|.
name|toException
argument_list|(
name|pair
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Got exception writing labels"
argument_list|,
name|t
argument_list|)
expr_stmt|;
if|if
condition|(
name|t
operator|instanceof
name|VisibilityControllerNotReadyException
condition|)
block|{
name|vcInitialized
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"VisibilityController was not yet initialized"
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|vcInitialized
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
else|else
name|LOG
operator|.
name|debug
argument_list|(
literal|"new labels added: "
operator|+
name|resp
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|t
argument_list|)
throw|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|vcInitialized
operator|.
name|get
argument_list|()
condition|)
do|;
comment|// Scan the visibility label
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
name|VisibilityUtils
operator|.
name|SYSTEM_LABEL
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
try|try
init|(
name|Table
name|ht
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|LABELS_TABLE_NAME
argument_list|)
init|;
name|ResultScanner
name|scanner
operator|=
name|ht
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
init|)
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|Result
name|next
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|next
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|i
operator|++
expr_stmt|;
block|}
block|}
comment|// One label is the "system" label.
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"The count should be 13"
argument_list|,
literal|13
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListLabels
parameter_list|()
throws|throws
name|Throwable
block|{
name|PrivilegedExceptionAction
argument_list|<
name|ListLabelsResponse
argument_list|>
name|action
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|ListLabelsResponse
argument_list|>
argument_list|()
block|{
specifier|public
name|ListLabelsResponse
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|ListLabelsResponse
name|response
init|=
literal|null
decl_stmt|;
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|)
block|{
name|response
operator|=
name|VisibilityClient
operator|.
name|listLabels
argument_list|(
name|conn
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Should not have thrown exception"
argument_list|)
expr_stmt|;
block|}
comment|// The addLabels() in setup added:
comment|// { SECRET, TOPSECRET, CONFIDENTIAL, PUBLIC, PRIVATE, COPYRIGHT, ACCENT,
comment|//  UNICODE_VIS_TAG, UC1, UC2 };
comment|// The previous tests added 2 more labels: ABC, XYZ
comment|// The 'system' label is excluded.
name|List
argument_list|<
name|ByteString
argument_list|>
name|labels
init|=
name|response
operator|.
name|getLabelList
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|12
argument_list|,
name|labels
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|labels
operator|.
name|contains
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|SECRET
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|labels
operator|.
name|contains
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|TOPSECRET
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|labels
operator|.
name|contains
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|CONFIDENTIAL
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|labels
operator|.
name|contains
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
literal|"ABC"
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|labels
operator|.
name|contains
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
literal|"XYZ"
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|labels
operator|.
name|contains
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|SYSTEM_LABEL
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListLabelsWithRegEx
parameter_list|()
throws|throws
name|Throwable
block|{
name|PrivilegedExceptionAction
argument_list|<
name|ListLabelsResponse
argument_list|>
name|action
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|ListLabelsResponse
argument_list|>
argument_list|()
block|{
specifier|public
name|ListLabelsResponse
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|ListLabelsResponse
name|response
init|=
literal|null
decl_stmt|;
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|)
block|{
name|response
operator|=
name|VisibilityClient
operator|.
name|listLabels
argument_list|(
name|conn
argument_list|,
literal|".*secret"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Should not have thrown exception"
argument_list|)
expr_stmt|;
block|}
comment|// Only return the labels that end with 'secret'
name|List
argument_list|<
name|ByteString
argument_list|>
name|labels
init|=
name|response
operator|.
name|getLabelList
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|labels
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|labels
operator|.
name|contains
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|SECRET
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|labels
operator|.
name|contains
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|TOPSECRET
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

